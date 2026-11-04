package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.VehicleSensor;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem: random-walk search, following turn commands, obstacle avoidance
 * with stuck recovery, and halting with a flag once the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.WAITING;
    private Angle a = Angle.Front;
    @RoboChartType("real") private double d0 = 0.0;
    @RoboChartType("real") private double d1 = 0.0;
    private Loc l = Loc.front;
    @RoboChartType("nat") private long evasionStart = 0;

    private final VehicleSensor vehicleSensor;
    private final VehicleActuator vehicle;
    private final Clock timeSource;

    /** Creates the controller in its initial mode, Waiting, searching by random walk. */
    public MovementController(VehicleSensor vehicleSensor, VehicleActuator vehicle, Clock timeSource) {
        this.vehicleSensor = vehicleSensor;
        this.vehicle = vehicle;
        this.timeSource = timeSource;
        vehicle.randomWalk();
    }

    /** The current mode. */
    public MovementMode currentMode() {
        return currentMode;
    }

    /** One control cycle; {@code event} is the event of this cycle, or null if there is none. */
    public void step(InputEvent event) {
        boolean evasionBelowStuckPeriod = timeSource.now() - evasionStart < DetectorConstants.STUCK_PERIOD;
        boolean advanceAboveStuckDist = d1 - d0 > DetectorConstants.STUCK_DIST;
        boolean makingProgress = evasionBelowStuckPeriod || advanceAboveStuckDist;
        boolean alwaysEnabled = true;

        if (currentMode == MovementMode.WAITING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }

        } else if (currentMode == MovementMode.GOING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING;
                l = obstacleEvent.side();
                evasionStart = timeSource.now();
                d0 = vehicleSensor.odometer();
                vehicle.changeDirection(l);
                timeSource.delay(DetectorConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }

        } else if (currentMode == MovementMode.FOUND) {
            if (alwaysEnabled) {
                currentMode = MovementMode.HALTED;
            }

        } else if (currentMode == MovementMode.AVOIDING) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }

        } else if (currentMode == MovementMode.TRYING_AGAIN) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.TRYING_AGAIN;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle obstacleEvent = (InputEvent.Obstacle) event;
                currentMode = MovementMode.AVOIDING_AGAIN;
                l = obstacleEvent.side();
                d1 = vehicleSensor.odometer();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
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
                evasionStart = timeSource.now();
                d0 = vehicleSensor.odometer();
                vehicle.changeDirection(l);
                timeSource.delay(DetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GETTING_OUT;
                vehicle.shortRandomWalk();
                timeSource.delay(DetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GETTING_OUT) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.FOUND;
                vehicle.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn turnEvent = (InputEvent.Turn) event;
                currentMode = MovementMode.GOING;
                a = turnEvent.direction();
                vehicle.move(DetectorConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.WAITING;
                vehicle.randomWalk();
            }

        } else if (currentMode == MovementMode.HALTED) {
            // Final mode: the robot stays halted after flagging the source.
        }
    }
}
