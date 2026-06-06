package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.MovementMode;
import chemdetector.sensor.Clock;

/**
 * Movement subsystem (CD-ARCH2): performs random-walk search, follows
 * direction commands from the gas-analysis subsystem, avoids obstacles,
 * and recovers when stuck.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    /** Most recent commanded direction (CD-MV-Var1). */
    private Angle a = Angle.Front;
    /** Odometer reading at the first obstacle of an evasion sequence (CD-MV-Var2). */
    @RoboChartType("real")
    private double d0;
    /** Odometer reading at the second obstacle of an evasion sequence (CD-MV-Var3). */
    @RoboChartType("real")
    private double d1;
    /** Side of the most recently detected obstacle (CD-MV-Var4). */
    private Loc l = Loc.front;
    /** Stuck-detection clock T (CD-MV-Clock1); reset via timer.nowMs(). */
    private long tStuck;

    private final Vehicle vehicle;
    private final Actuator actuator;
    private final Clock timer;

    public MovementController(Vehicle vehicle, Actuator actuator, Clock timer) {
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.timer = timer;
    }

    public MovementMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean withinStuckPeriod = timer.nowMs() - tStuck < ChemConstants.STUCK_PERIOD;
        boolean beyondStuckDist = d1 - d0 > ChemConstants.STUCK_DIST;
        boolean makingProgress = withinStuckPeriod || beyondStuckDist;

        if (currentMode == MovementMode.Waiting) {
            vehicle.randomWalk();
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.value();
                currentMode = MovementMode.Avoiding;
                tStuck = timer.nowMs();
                d0 = vehicle.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, a);
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
                a = t.value();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }

        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle o = (InputEvent.Obstacle) event;
                l = o.value();
                currentMode = MovementMode.AvoidingAgain;
                d1 = vehicle.odometer();
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(ChemConstants.LV, a);
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
                tStuck = timer.nowMs();
                d0 = vehicle.odometer();
                vehicle.changeDirection(l);
                vehicle.pause(ChemConstants.EVADE_TIME);
            } else if (!makingProgress) {
                currentMode = MovementMode.GettingOut;
                vehicle.shortRandomWalk();
                vehicle.pause(ChemConstants.OUT_PERIOD);
            }

        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn t = (InputEvent.Turn) event;
                a = t.value();
                currentMode = MovementMode.Going;
                vehicle.move(ChemConstants.LV, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
            }
        }
    }
}
