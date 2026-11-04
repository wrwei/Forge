package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.OdometerSensor;

/**
 * Movement subsystem: random-walk search, travel in the commanded direction, obstacle avoidance
 * with stuck recovery, and the final halt with the flag signal.
 */
public final class MovementController {
    private MovementMode currentMode = MovementMode.WAITING;
    private final OdometerSensor odometerSensor;
    private final Clock clock;
    private final Vehicle vehicle;

    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;

    /** Creates the subsystem with its odometer, clock and Vehicle. */
    public MovementController(OdometerSensor odometerSensor, Clock clock, Vehicle vehicle) {
        this.odometerSensor = odometerSensor;
        this.clock = clock;
        this.vehicle = vehicle;
    }

    /** The current state. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** Performs one control cycle; {@code event} is the event received in this cycle, if any. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = clock.now() - evasionStart < ChemicalDetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > ChemicalDetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.WAITING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnEvent.heading();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnEvent.heading();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING;
                l = obstacleEvent.side();
                evasionStart = clock.now();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            currentMode = MovementMode.HALTED;

        } else if (currentMode == MovementMode.AVOIDING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnEvent.heading();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnEvent.heading();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING_AGAIN;
                l = obstacleEvent.side();
                d1 = odometerSensor.odometer();
            }

        } else if (currentMode == MovementMode.AVOIDING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (makingProgress) {
                currentMode = MovementMode.AVOIDING;
                evasionStart = clock.now();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnEvent.heading();
                vehicle.move(ChemicalDetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.HALTED) {
            // Terminal: the robot stays halted after the source has been found.
        }
    }
}
