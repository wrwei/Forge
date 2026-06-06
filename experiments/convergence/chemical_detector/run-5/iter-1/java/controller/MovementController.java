package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.MovementEvent;
import chemdetector.event.VehicleEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;

/**
 * Movement subsystem: performs random-walk search, follows direction
 * commands, handles obstacle avoidance, and recovers when stuck
 * (CD-ARCH2).
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;
    private final Vehicle vehicle;
    private final Clock clk;

    private Angle a = Angle.Front;
    private Loc l = Loc.front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    private long tEvade;

    public MovementController(Vehicle vehicle, Clock clk) {
        this.vehicle = vehicle;
        this.clk = clk;
    }

    public void step(MovementEvent event) {
        boolean withinStuckWindow = clk.nowMs() - this.tEvade < ChemConstants.STUCK_PERIOD;
        boolean escapedByDistance = this.d1 - this.d0 > ChemConstants.STUCK_DIST;
        boolean makingProgress = withinStuckWindow || escapedByDistance;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof MovementEvent.Stop) {
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.value();
                vehicle.move(ChemConstants.LV, this.a);
                currentMode = MovementMode.Going;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.Going) {
            if (event instanceof MovementEvent.Stop) {
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.value();
                currentMode = MovementMode.Going;
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.value();
                this.tEvade = clk.nowMs();
                this.d0 = vehicle.odometer();
                vehicle.changeDirection(new VehicleEvent.ChangeDirection(this.l));
                vehicle.pause(ChemConstants.EVADE_TIME);
                currentMode = MovementMode.Avoiding;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof MovementEvent.Stop) {
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.value();
                vehicle.move(ChemConstants.LV, this.a);
                currentMode = MovementMode.TryingAgain;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof MovementEvent.Stop) {
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.value();
                currentMode = MovementMode.TryingAgain;
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.value();
                this.d1 = vehicle.odometer();
                currentMode = MovementMode.AvoidingAgain;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof MovementEvent.Stop) {
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                this.tEvade = clk.nowMs();
                this.d0 = vehicle.odometer();
                vehicle.changeDirection(new VehicleEvent.ChangeDirection(this.l));
                vehicle.pause(ChemConstants.EVADE_TIME);
                currentMode = MovementMode.Avoiding;
            } else if (!makingProgress) {
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
                currentMode = MovementMode.GettingOut;
            }
        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof MovementEvent.Stop) {
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.value();
                vehicle.move(ChemConstants.LV, this.a);
                currentMode = MovementMode.Going;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        } else if (currentMode == MovementMode.Found) {
            if (event instanceof MovementEvent.Stop) {
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.value();
                currentMode = MovementMode.Found;
            } else if (event instanceof MovementEvent.Resume) {
                currentMode = MovementMode.Found;
            }
        }
    }

    public MovementMode currentMode() {
        return currentMode;
    }
}
