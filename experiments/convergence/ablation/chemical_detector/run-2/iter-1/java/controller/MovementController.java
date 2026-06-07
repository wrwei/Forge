package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.OdometerSensor;

/**
 * Movement subsystem (CD-ARCH2): performs random-walk search, follows
 * direction commands, handles obstacle avoidance, and recovers when
 * stuck. Emits the flag event to the Vehicle when the source is found.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final OdometerSensor odometerSensor;
    private final Actuator actuator;
    private final Clock clock;

    /** Most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;
    /** Distance sampled when the current evasion sequence began (CD-MV-Var2). */
    @RoboChartType("real")
    private double d0 = 0.0;
    /** Distance sampled at the second obstacle of an evasion sequence (CD-MV-Var3). */
    @RoboChartType("real")
    private double d1 = 0.0;
    /** Side of the most recent obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;
    /** Start of the current evasion sequence (CD-MV-Clock1). */
    private long evasionStart = 0L;

    public MovementController(Vehicle vehicle, OdometerSensor odometerSensor,
                              Actuator actuator, Clock clock) {
        this.vehicle = vehicle;
        this.odometerSensor = odometerSensor;
        this.actuator = actuator;
        this.clock = clock;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = clock.nowMs() - this.evasionStart < Constants.stuckPeriod;
        boolean advancedBeyondStuckDist = this.d1 - this.d0 > Constants.stuckDist;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.Avoiding;
                this.d0 = odometerSensor.distance();
                this.evasionStart = clock.nowMs();
                vehicle.changeDirection(this.l);
                vehicle.pause(Constants.evadeTime);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, this.a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.AvoidingAgain;
                this.d1 = odometerSensor.distance();
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
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
                this.d0 = odometerSensor.distance();
                this.evasionStart = clock.nowMs();
                vehicle.changeDirection(this.l);
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
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, this.a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
