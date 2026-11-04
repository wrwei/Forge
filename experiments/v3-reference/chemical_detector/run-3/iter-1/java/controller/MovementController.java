package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem (CD-ARCH2): random-walk search, following turn
 * commands, obstacle avoidance with stuck detection and recovery, and halting
 * with the flag once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;
    private final OdometerSensor odometerSensor;
    private final VehicleActuator vehicle;
    private final Clock timer;

    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    private long evasionStart = 0;

    /**
     * Creates the subsystem in its initial state, Waiting (CD-MV-Beh1), and
     * starts Waiting's random-walk search.
     *
     * @param odometerSensor the Vehicle's odometer
     * @param vehicle the Vehicle's actuation surface
     * @param timer the time source of the evasion clock
     */
    public MovementController(OdometerSensor odometerSensor, VehicleActuator vehicle, Clock timer) {
        this.odometerSensor = odometerSensor;
        this.vehicle = vehicle;
        this.timer = timer;
        vehicle.randomWalk();
    }

    /**
     * The current state.
     *
     * @return the current mode
     */
    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle: takes at most one transition.
     *
     * @param event the event of this cycle, if any
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
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
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
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
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING;
                l = obstacleEvent.side();
                d0 = odometerSensor.odometer();
                evasionStart = timer.now();
                vehicle.changeDirection(l);
                timer.delay(DetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.FOUND) {
            // Terminal: the robot stays halted.

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
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
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
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
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
                d0 = odometerSensor.odometer();
                evasionStart = timer.now();
                vehicle.changeDirection(l);
                timer.delay(DetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                timer.delay(DetectorConstants.OUT_PERIOD);
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
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            }
        }
    }
}
