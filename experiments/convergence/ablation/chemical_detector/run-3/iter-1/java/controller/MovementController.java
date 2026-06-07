package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.domain.Angle;
import chemdetector.domain.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.sensor.Clock;
import chemdetector.sensor.OdometerSensor;

/**
 * Movement subsystem: random-walk search, directed travel, obstacle
 * avoidance, and stuck recovery (CD-ARCH2, CD-MV-FR1..7, CD-MV-Beh1..23).
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final OdometerSensor odometerSensor;
    private final Clock clock;
    private final Actuator actuator;

    /** Most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;
    /** Distance sampled when the current evasion sequence began (CD-MV-Var2). */
    @RoboChartType("real")
    private double d0 = 0.0;
    /** Distance sampled when a second obstacle was hit (CD-MV-Var3). */
    @RoboChartType("real")
    private double d1 = 0.0;
    /** Side of the most recent obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;
    /** Start of the current evasion sequence (CD-MV-Clock1). */
    private long evasionTimer = 0L;

    public MovementController(Vehicle vehicle, OdometerSensor odometerSensor,
            Clock clock, Actuator actuator) {
        this.vehicle = vehicle;
        this.odometerSensor = odometerSensor;
        this.clock = clock;
        this.actuator = actuator;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = clock.nowMs() - evasionTimer < Constants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > Constants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || advancedBeyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle ob = (InputEvent.Obstacle) event;
                l = ob.side();
                currentMode = MovementMode.Avoiding;
                d0 = odometerSensor.odometer();
                evasionTimer = clock.nowMs();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle ob = (InputEvent.Obstacle) event;
                l = ob.side();
                currentMode = MovementMode.AvoidingAgain;
                d1 = odometerSensor.odometer();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.LV, a);
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
                d0 = odometerSensor.odometer();
                evasionTimer = clock.nowMs();
                vehicle.changeDirection(l);
                vehicle.pause(Constants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
