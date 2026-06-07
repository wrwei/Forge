package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.MovementEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;
import chemdetector.sensor.Sensor;

/**
 * Movement subsystem: random-walk search, direction following, obstacle
 * avoidance, and stuck recovery.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Sensor sensor;
    private final Vehicle vehicle;
    private final Clock timer;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0 = 0.0;
    @RoboChartType("real")
    private double d1 = 0.0;
    private long evadeStart = 0L;

    public MovementController(Sensor sensor, Vehicle vehicle, Clock timer) {
        this.sensor = sensor;
        this.vehicle = vehicle;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(MovementEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - this.evadeStart < ChemConstants.STUCK_PERIOD;
        boolean advancedBeyondStuckDist = this.d1 - this.d0 > ChemConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, this.a);
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
                this.a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.Avoiding;
                this.d0 = sensor.currentDistance();
                this.evadeStart = timer.nowMs();
                vehicle.changeDirection(this.l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemConstants.LV, this.a);
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
                this.a = t.direction();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.side();
                currentMode = MovementMode.AvoidingAgain;
                this.d1 = sensor.currentDistance();
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
                this.d0 = sensor.currentDistance();
                this.evadeStart = timer.nowMs();
                vehicle.changeDirection(this.l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (!withinStuckPeriod && !advancedBeyondStuckDist) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.direction();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
