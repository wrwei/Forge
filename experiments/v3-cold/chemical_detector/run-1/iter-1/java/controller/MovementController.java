package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem: random-walk search, travel in the commanded
 * direction, obstacle avoidance with stuck recovery, and the final halt.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;

    private final OdometerSensor odometerSensor;
    private final VehicleActuator vehicle;
    private final Clock timer;

    /**
     * @param odometerSensor the Vehicle's odometer
     * @param vehicle the Vehicle's movement operations
     * @param timer the time source of the evasion timing
     */
    public MovementController(OdometerSensor odometerSensor, VehicleActuator vehicle, Clock timer) {
        this.odometerSensor = odometerSensor;
        this.vehicle = vehicle;
        this.timer = timer;
    }

    /**
     * One control cycle of the movement subsystem.
     *
     * @param event the event received this cycle, or null if there is none
     */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleEvent.side();
                evasionStart = timer.now();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                timer.pause(DetectorConstants.EVADE_TIME);
            }

        } else if (currentMode == MovementMode.Found) {
            currentMode = MovementMode.Halted;

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleEvent.side();
                d1 = odometerSensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                evasionStart = timer.now();
                d0 = odometerSensor.odometer();
                vehicle.changeDirection(l);
                timer.pause(DetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                timer.pause(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(DetectorConstants.HALT_VELOCITY, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            }

        } else if (currentMode == MovementMode.Halted) {
            // Terminal mode: the source has been found and the Vehicle stays halted.
        }
    }
}
