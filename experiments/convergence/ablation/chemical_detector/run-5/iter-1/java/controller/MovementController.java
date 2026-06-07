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
import chemdetector.sensor.MoveSensor;
import chemdetector.time.Clock;

/**
 * Movement subsystem (CD-ARCH2): random-walk search, direction-command
 * following, obstacle avoidance, and stuck recovery.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final Actuator actuator;
    private final MoveSensor moveSensor;
    private final Clock timer;

    /** Most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;

    /** Distance sampled when the current evasion sequence began (CD-MV-Var2). */
    @RoboChartType("real")
    private double d0;

    /** Distance sampled at the second obstacle of an evasion (CD-MV-Var3). */
    @RoboChartType("real")
    private double d1;

    /** Side of the most recent obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;

    /** Start of the current evasion sequence (CD-MV-Clock1). */
    private long evasionStartMs;

    public MovementController(Vehicle vehicle, Actuator actuator,
            MoveSensor moveSensor, Clock timer) {
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.moveSensor = moveSensor;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - evasionStartMs < Constants.stuckPeriod;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.stuckDist;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.side();
                currentMode = MovementMode.Avoiding;
                d0 = moveSensor.odometer();
                evasionStartMs = timer.nowMs();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.evadeTime);
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.side();
                currentMode = MovementMode.AvoidingAgain;
                d1 = moveSensor.odometer();
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                currentMode = MovementMode.Avoiding;
                d0 = moveSensor.odometer();
                evasionStartMs = timer.nowMs();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.evadeTime);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.outPeriod);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.angle();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }
        }
    }
}
