package chemdetector.controller.movement;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Loc;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.operation.ChangeDirection;
import chemdetector.sensor.SystemClock;
import chemdetector.vehicle.Vehicle;

/**
 * Movement subsystem: performs random-walk search, follows direction
 * commands, handles obstacle avoidance, and recovers from stuck
 * conditions.
 *
 * Mode-nested single-method state machine, per the codegen rules.
 */
public final class MovementController {

    private MovementMode currentMode = MovementMode.Waiting;

    private final Vehicle vehicle;
    private final Actuator actuator;
    private final ChangeDirection changeDir;
    private final SystemClock tick;

    // State variables (CD-MV-Var1..4)
    private Angle a = Angle.Front;
    @RoboChartType("real")
    private double d0;
    @RoboChartType("real")
    private double d1;
    private Loc l = Loc.front;

    // Clock T (CD-MV-Clock1) — stored as the tick timestamp of the last reset
    @RoboChartType("nat")
    private int T;

    // Most recent odometer reading provided by the Vehicle.
    @RoboChartType("real")
    private double lastOdometer;

    public MovementController(Vehicle vehicle, Actuator actuator,
                              ChangeDirection changeDir, SystemClock tick) {
        this.vehicle = vehicle;
        this.actuator = actuator;
        this.changeDir = changeDir;
        this.tick = tick;
        this.T = this.tick.nowMs();
        // Initial state: Waiting — randomWalk during action begins immediately.
        this.vehicle.randomWalk();
    }

    public MovementMode currentMode() { return currentMode; }
    public Angle a() { return a; }
    public double d0() { return d0; }
    public double d1() { return d1; }
    public Loc l() { return l; }

    public void step(InputEvent event) {
        // Update the cached odometer reading when an Odometer event arrives.
        if (event instanceof InputEvent.Odometer) {
            InputEvent.Odometer oe = (InputEvent.Odometer) event;
            lastOdometer = oe.payload();
        }

        // --- Named boolean predicates ---
        boolean sinceTBelowPeriod = tick.nowMs() - T < Constants.stuckPeriod;
        boolean distAboveStuckDist = d1 - d0 > Constants.stuckDist;
        boolean makingProgress = sinceTBelowPeriod || distAboveStuckDist;
        boolean stuck = !sinceTBelowPeriod && !distAboveStuckDist;

        if (currentMode == MovementMode.Waiting) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                // Entry actions of Found: flag; move(0, Front).
                actuator.apply(new OutputEvent.Flag());
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.Going;
                // Entry action of Going: move(lv, a).
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                // Re-enter randomWalk during-action.
                vehicle.randomWalk();
            }
        } else if (currentMode == MovementMode.Going) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.Going;
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle oe = (InputEvent.Obstacle) event;
                l = oe.payload();
                currentMode = MovementMode.Avoiding;
                // Transition action: reset clock T.
                T = tick.nowMs();
                // Entry actions of Avoiding: odometer ? d0; changeDirection(l); wait(evadeTime).
                d0 = lastOdometer;
                changeDir.setL(l);
                changeDir.compute();
                vehicle.pause(Constants.evadeTime);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
        } else if (currentMode == MovementMode.Found) {
            // Autonomous transition to Final; no further outputs needed.
            currentMode = MovementMode.Final;
        } else if (currentMode == MovementMode.Avoiding) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.TryingAgain;
                // Entry action of TryingAgain: move(lv, a).
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
        } else if (currentMode == MovementMode.TryingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.TryingAgain;
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle oe = (InputEvent.Obstacle) event;
                l = oe.payload();
                // Transition action: odometer ? d1.
                d1 = lastOdometer;
                currentMode = MovementMode.AvoidingAgain;
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
        } else if (currentMode == MovementMode.AvoidingAgain) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            } else if (makingProgress) {
                // Autonomous guarded: back to Avoiding (progress made).
                currentMode = MovementMode.Avoiding;
                // Transition action: reset clock T.
                T = tick.nowMs();
                // Re-enter Avoiding's entry actions.
                d0 = lastOdometer;
                changeDir.setL(l);
                changeDir.compute();
                vehicle.pause(Constants.evadeTime);
            } else if (stuck) {
                // Autonomous guarded: to GettingOut (stuck detected).
                currentMode = MovementMode.GettingOut;
                // Entry actions of GettingOut: shortRandomWalk(); wait(outPeriod).
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.outPeriod);
            }
        } else if (currentMode == MovementMode.GettingOut) {
            if (event instanceof InputEvent.Stop) {
                currentMode = MovementMode.Found;
                actuator.apply(new OutputEvent.Flag());
                vehicle.flag();
                vehicle.move(0.0, Angle.Front);
            } else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.payload();
                currentMode = MovementMode.Going;
                // Entry action of Going: move(lv, a).
                vehicle.move(Constants.lv, a);
            } else if (event instanceof InputEvent.Resume) {
                currentMode = MovementMode.Waiting;
                vehicle.randomWalk();
            }
        } else if (currentMode == MovementMode.Final) {
            // Terminal state.
        }
    }
}
