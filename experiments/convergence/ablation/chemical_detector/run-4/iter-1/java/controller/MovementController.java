package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.CdConstants;
import chemdetector.data.Loc;
import chemdetector.event.MovementEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;

/**
 * The movement subsystem: random-walk search, direction following,
 * obstacle avoidance, and stuck recovery. Consumes turn/stop/resume from
 * the gas-analysis subsystem and obstacle events from the Vehicle.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final Clock clock;

    /** Most recent commanded direction. */
    private Angle a = Angle.Front;
    /** Distance sampled when the current evasion sequence began. */
    @RoboChartType("real")
    private double d0;
    /** Distance sampled when a second obstacle was hit. */
    @RoboChartType("real")
    private double d1;
    /** Side of the most recent obstacle. */
    private Loc l = Loc.front;
    /** Start of the current evasion sequence (clock sample). */
    private long evadeStart;

    public MovementController(Vehicle vehicle, Clock clock) {
        this.vehicle = vehicle;
        this.clock = clock;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all movement transitions for one control cycle.
     * Event-triggered transitions consume {@code event}; autonomous
     * transitions fire on guards alone.
     */
    public void step(MovementEvent event) {
        // --- Named boolean predicates ---
        boolean withinStuckPeriod = clock.nowMs() - evadeStart < CdConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = d1 - d0 > CdConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.dir();
                currentMode = MovementMode.Going;
                vehicle.move(CdConstants.LV, a);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.dir();
                currentMode = MovementMode.Going;
                vehicle.move(CdConstants.LV, a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle ob = (MovementEvent.Obstacle) event;
                this.l = ob.side();
                currentMode = MovementMode.Avoiding;
                this.d0 = vehicle.odometer();
                this.evadeStart = clock.nowMs();
                vehicle.changeDirection(CdConstants.LV, l);
                vehicle.pause(CdConstants.EVADE_TIME);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.dir();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(CdConstants.LV, a);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.dir();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(CdConstants.LV, a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle ob = (MovementEvent.Obstacle) event;
                this.l = ob.side();
                currentMode = MovementMode.AvoidingAgain;
                this.d1 = vehicle.odometer();
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (withinStuckPeriod || advancedBeyondStuckDist) {
                currentMode = MovementMode.Avoiding;
                this.d0 = vehicle.odometer();
                this.evadeStart = clock.nowMs();
                vehicle.changeDirection(CdConstants.LV, l);
                vehicle.pause(CdConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(CdConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.dir();
                currentMode = MovementMode.Going;
                vehicle.move(CdConstants.LV, a);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
            }
        }
    }
}
