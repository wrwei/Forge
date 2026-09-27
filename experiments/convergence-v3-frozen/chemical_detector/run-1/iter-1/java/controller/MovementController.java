package chemical_detector.controller;

import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemicalDetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.Loc;
import chemical_detector.event.MovementEvent;
import chemical_detector.mode.MovementMode;
import chemical_detector.timing.Clock;

/**
 * The movement subsystem (CD-ARCH2): it random-walks while searching, follows
 * the direction commands issued by the gas-analysis subsystem, avoids
 * obstacles, recovers when stuck, and flags the Vehicle once the chemical
 * source has been confirmed.
 */
public final class MovementController {

    private final Vehicle vehicle;

    private final Clock timer;

    private MovementMode currentMode = MovementMode.Waiting;

    private Angle a = Angle.Front;

    private Loc l = Loc.front;

    @RoboChartType("real")
    private double d0 = 0.0;

    @RoboChartType("real")
    private double d1 = 0.0;

    @RoboChartType("nat")
    private long evadeStart = 0L;

    public MovementController(Vehicle vehicle, Clock timer) {
        this.vehicle = vehicle;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return this.currentMode;
    }

    public void step(MovementEvent event) {
        boolean makingProgress =
                timer.nowMs() - this.evadeStart < ChemicalDetectorConstants.STUCK_PERIOD
                || this.d1 - this.d0 > ChemicalDetectorConstants.STUCK_DIST;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.a();
                this.currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.a();
                this.currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.l();
                this.currentMode = MovementMode.Avoiding;
                this.d0 = vehicle.odometer();
                this.evadeStart = timer.nowMs();
                vehicle.changeDirection(this.l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Found) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
            }

        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.a();
                this.currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.a();
                this.currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemicalDetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Obstacle) {
                MovementEvent.Obstacle o = (MovementEvent.Obstacle) event;
                this.l = o.l();
                this.currentMode = MovementMode.AvoidingAgain;
                this.d1 = vehicle.odometer();
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.Waiting;
            } else if (makingProgress) {
                this.currentMode = MovementMode.Avoiding;
                this.d0 = vehicle.odometer();
                this.evadeStart = timer.nowMs();
                vehicle.changeDirection(this.l);
                vehicle.pause(ChemicalDetectorConstants.EVADE_TIME);
            } else if (!makingProgress) {
                this.currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemicalDetectorConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof MovementEvent.Stop) {
                this.currentMode = MovementMode.Found;
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof MovementEvent.Turn) {
                MovementEvent.Turn t = (MovementEvent.Turn) event;
                this.a = t.a();
                this.currentMode = MovementMode.Going;
                vehicle.move(ChemicalDetectorConstants.LV, this.a);
            } else if (event instanceof MovementEvent.Resume) {
                this.currentMode = MovementMode.Waiting;
            }
        }
    }
}
