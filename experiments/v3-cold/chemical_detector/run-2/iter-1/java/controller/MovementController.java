package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.EventBus;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction,
 * obstacle avoidance with stuck detection and recovery, and halting with the
 * flag signal once the source is found.
 */
public final class MovementController {
    private MovementMode currentMode = MovementMode.WAITING;
    private final OdometerSensor odometer;
    private final Vehicle vehicle;
    private final Clock timer;
    private final EventBus bus;

    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;

    /**
     * Creates the subsystem in its initial mode, Waiting.
     *
     * @param odometer the distance-travelled sensor
     * @param vehicle the Vehicle's movement operations
     * @param timer the time source
     * @param bus where flag is sent
     */
    public MovementController(OdometerSensor odometer, Vehicle vehicle, Clock timer, EventBus bus) {
        this.odometer = odometer;
        this.vehicle = vehicle;
        this.timer = timer;
        this.bus = bus;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or null if none
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.WAITING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                bus.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                bus.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleReport = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING;
                l = obstacleReport.side();
                evasionStart = timer.now();
                d0 = odometer.distanceTravelled();
                vehicle.changeDirection(l);
                timer.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            // Terminal: halted after sending flag.

        } else if (currentMode == MovementMode.AVOIDING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                bus.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnCommand.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                bus.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnCommand.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleReport = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING_AGAIN;
                l = obstacleReport.side();
                d1 = odometer.distanceTravelled();
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                bus.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (makingProgress) {
                currentMode = MovementMode.AVOIDING;
                evasionStart = timer.now();
                d0 = odometer.distanceTravelled();
                vehicle.changeDirection(l);
                timer.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                timer.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                bus.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnCommand = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnCommand.direction();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }
        }
    }
}
