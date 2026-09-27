package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;

/**
 * Movement subsystem (CD-ARCH2): random-walk search, following turn commands, obstacle
 * avoidance with stuck recovery, and halting with a single {@code flag} once the source
 * is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Sensor sensor;

    private final Vehicle vehicle;

    private final ChangeDirection evasion;

    private final Actuator actuator;

    private final Clock timer;

    private Angle a = Angle.Front;

    @RoboChartType("real")
    private double d0;

    @RoboChartType("real")
    private double d1;

    private Loc l = Loc.front;

    @RoboChartType("nat")
    private long T;

    public MovementController(Sensor sensor, Vehicle vehicle, ChangeDirection evasion,
                              Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.evasion = evasion;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /** Performs at most one transition of the movement state machine. */
    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - T < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                var turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                var turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                var obstacleEvent = (InputEvent.obstacle) event;
                currentMode = MovementMode.Avoiding;
                l = obstacleEvent.side();
                T = timer.nowMs();
                d0 = sensor.odometer();
                evasion.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.obstacle) {
                var obstacleEvent = (InputEvent.obstacle) event;
                currentMode = MovementMode.Found;
                l = obstacleEvent.side();
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                var turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                var turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.TryingAgain;
                a = turnEvent.direction();
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.obstacle) {
                var obstacleEvent = (InputEvent.obstacle) event;
                currentMode = MovementMode.AvoidingAgain;
                l = obstacleEvent.side();
                d1 = sensor.odometer();
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                T = timer.nowMs();
                d0 = sensor.odometer();
                evasion.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.turn) {
                var turnEvent = (InputEvent.turn) event;
                currentMode = MovementMode.Going;
                a = turnEvent.direction();
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
