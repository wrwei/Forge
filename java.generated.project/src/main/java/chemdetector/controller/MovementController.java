package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Timer;

/**
 * Movement subsystem (CD-ARCH2): performs random-walk search, follows
 * direction commands, handles obstacle avoidance, and recovers when
 * stuck. Emits the flag event to the Vehicle on entering Found.
 *
 * <p>Single-method, mode-nested if-else state machine over
 * {@link MovementMode}. Waiting is the initial state; Final is the
 * terminal state j1.</p>
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final Timer timer;

    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private Loc l = Loc.front;
    private long T = 0L;

    public MovementController(Vehicle vehicle, Actuator actuator, Timer timer) {
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean makingProgress =
            timer.nowMs() - T < Constants.stuckPeriod || d1 - d0 > Constants.stuckDist;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                vehicle.move(Constants.lv, a);
                currentMode = MovementMode.Going;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.loc();
                T = timer.nowMs();
                d0 = vehicle.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.evadeTime);
                currentMode = MovementMode.Avoiding;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                vehicle.move(Constants.lv, a);
                currentMode = MovementMode.Going;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.Found) {
            currentMode = MovementMode.Final;
        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                vehicle.move(Constants.lv, a);
                currentMode = MovementMode.TryingAgain;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.loc();
                d1 = vehicle.odometer();
                currentMode = MovementMode.AvoidingAgain;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                vehicle.move(Constants.lv, a);
                currentMode = MovementMode.TryingAgain;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                T = timer.nowMs();
                d0 = vehicle.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.evadeTime);
                currentMode = MovementMode.Avoiding;
            } else {
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.outPeriod);
                currentMode = MovementMode.GettingOut;
            }
        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                vehicle.move(Constants.lv, a);
                currentMode = MovementMode.Going;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.Final) {
            if (event instanceof InputEvent.Tick) {
                currentMode = MovementMode.Final;
            }
        }
    }
}
