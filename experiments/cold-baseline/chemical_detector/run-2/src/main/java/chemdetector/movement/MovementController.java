package chemdetector.movement;

import chemdetector.annotation.RoboChartType;
import chemdetector.clock.StuckClock;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.Loc;
import chemdetector.movement.event.InputEvent;
import chemdetector.movement.event.OutputEvent;
import chemdetector.movement.mode.MVMode;
import chemdetector.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.List;

/**
 * CD-ARCH2 movement subsystem. Single-method, mode-nested if-else
 * state machine over the modes in {@link MVMode}.
 *
 * <p>Modes: Waiting | Going | Avoiding | TryingAgain | AvoidingAgain
 * | GettingOut | Found | Final.
 *
 * <p>Inputs: {@link InputEvent.Turn} / {@link InputEvent.Stop} /
 * {@link InputEvent.Resume} from the gas-analysis subsystem;
 * {@link InputEvent.Obstacle} / {@link InputEvent.Odometer} from the
 * Vehicle. Outputs: {@link OutputEvent.Flag} to the Vehicle on entry
 * to Found.
 */
public final class MovementController {

    private final Vehicle vehicle;
    private final StuckClock clock;
    private final List<OutputEvent> outbox = new ArrayList<>();

    private MVMode currentMode = MVMode.Waiting;

    /** CD-MV-Var1: most recent commanded heading. */
    private Angle a = Angle.Front;

    /** CD-MV-Var4: side of the most recent obstacle. */
    private Loc l = Loc.front;

    /** CD-MV-Var2: distance recorded on first obstacle of an evasion sequence. */
    @RoboChartType("real")
    private double d0 = 0.0;

    /** CD-MV-Var3: distance recorded on second obstacle of an evasion sequence. */
    @RoboChartType("real")
    private double d1 = 0.0;

    /** Latest odometer reading buffered from the most recent Odometer event. */
    @RoboChartType("real")
    private double currentDistance = 0.0;

    /**
     * CD-MV-Clock1: monotonic time-stamp of the last clock reset.
     * Stuck detection compares {@code clock.nowMs() - tStart} against
     * {@code stuckPeriod}; the ETL rewrites this to
     * {@code since(tStart) < stuckPeriod} when generating the
     * RoboChart model.
     */
    @RoboChartType("nat")
    private long tStart = 0;

    public MovementController(Vehicle vehicle, StuckClock clock) {
        this.vehicle = vehicle;
        this.clock = clock;
        // Initial state during action: randomWalk (CD-MV-FR1).
        vehicle.randomWalk();
    }

    public MVMode currentMode() {
        return currentMode;
    }

    public Angle a() {
        return a;
    }

    public Loc l() {
        return l;
    }

    public List<OutputEvent> drain() {
        List<OutputEvent> snapshot = new ArrayList<>(outbox);
        outbox.clear();
        return snapshot;
    }

    public void step(InputEvent event) {
        // Named boolean predicates declared BEFORE the if-else chain.
        boolean makingProgress = (clock.nowMs() - tStart < Constants.stuckPeriod)
                || (d1 - d0 > Constants.stuckDist);
        boolean stuck = (clock.nowMs() - tStart >= Constants.stuckPeriod)
                && (d1 - d0 <= Constants.stuckDist);

        if (currentMode == MVMode.Waiting) {
            // CD-MV-Beh4: on Stop -> Found (entry: flag, halt).
            if (event instanceof InputEvent.Stop) {
                outbox.add(new OutputEvent.Flag());
                vehicle.raiseFlag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MVMode.Found;
            }
            // CD-MV-Beh2: on Turn -> Going (entry: move(lv, a)).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                vehicle.move(Constants.lv, a);
                currentMode = MVMode.Going;
            }
            // CD-MV-Beh3: on Resume -> Waiting (self-loop).
            else if (event instanceof InputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }
            // Cache odometer readings while waiting.
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                currentDistance = oe.d();
            }
        } else if (currentMode == MVMode.Going) {
            // CD-MV-Beh6: on Stop -> Found.
            if (event instanceof InputEvent.Stop) {
                outbox.add(new OutputEvent.Flag());
                vehicle.raiseFlag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MVMode.Found;
            }
            // CD-MV-Beh8: on Resume -> Waiting.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }
            // CD-MV-Beh5: on Turn -> Going (self-loop, re-fire move).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                vehicle.move(Constants.lv, a);
                currentMode = MVMode.Going;
            }
            // CD-MV-Beh7: on Obstacle -> Avoiding (reset clock, then Avoiding entry).
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle oe = (InputEvent.Obstacle) event;
                l = oe.l();
                tStart = clock.nowMs();
                d0 = currentDistance;
                vehicle.changeDirection(l);
                vehicle.pause(Constants.evadeTime);
                currentMode = MVMode.Avoiding;
            }
            // Cache odometer readings during travel.
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                currentDistance = oe.d();
            }
        } else if (currentMode == MVMode.Avoiding) {
            // CD-MV-Beh11: on Stop -> Found.
            if (event instanceof InputEvent.Stop) {
                outbox.add(new OutputEvent.Flag());
                vehicle.raiseFlag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MVMode.Found;
            }
            // CD-MV-Beh12: on Resume -> Waiting.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }
            // CD-MV-Beh10: on Turn -> TryingAgain (entry: move(lv, a)).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                vehicle.move(Constants.lv, a);
                currentMode = MVMode.TryingAgain;
            }
            // Cache odometer to feed d0 on subsequent Avoiding entries.
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                d0 = oe.d();
                currentDistance = oe.d();
            }
        } else if (currentMode == MVMode.TryingAgain) {
            // CD-MV-Beh14: on Stop -> Found.
            if (event instanceof InputEvent.Stop) {
                outbox.add(new OutputEvent.Flag());
                vehicle.raiseFlag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MVMode.Found;
            }
            // CD-MV-Beh15: on Resume -> Waiting.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }
            // CD-MV-Beh13: on Turn -> TryingAgain (self-loop).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                vehicle.move(Constants.lv, a);
                currentMode = MVMode.TryingAgain;
            }
            // CD-MV-Beh16: on Obstacle -> AvoidingAgain (record d1).
            else if (event instanceof InputEvent.Obstacle) {
                InputEvent.Obstacle oe = (InputEvent.Obstacle) event;
                l = oe.l();
                d1 = currentDistance;
                currentMode = MVMode.AvoidingAgain;
            }
            else if (event instanceof InputEvent.Odometer) {
                InputEvent.Odometer oe = (InputEvent.Odometer) event;
                currentDistance = oe.d();
            }
        } else if (currentMode == MVMode.AvoidingAgain) {
            // CD-MV-Beh19: on Stop -> Found.
            if (event instanceof InputEvent.Stop) {
                outbox.add(new OutputEvent.Flag());
                vehicle.raiseFlag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MVMode.Found;
            }
            // CD-MV-Beh20: on Resume -> Waiting.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }
            // CD-MV-Beh17: making progress -> Avoiding (reset clock + Avoiding entry).
            else if (makingProgress) {
                tStart = clock.nowMs();
                d0 = currentDistance;
                vehicle.changeDirection(l);
                vehicle.pause(Constants.evadeTime);
                currentMode = MVMode.Avoiding;
            }
            // CD-MV-Beh18: stuck -> GettingOut (entry: shortRandomWalk, wait).
            else if (stuck) {
                vehicle.shortRandomWalk();
                vehicle.pause(Constants.outPeriod);
                currentMode = MVMode.GettingOut;
            }
        } else if (currentMode == MVMode.GettingOut) {
            // CD-MV-Beh22: on Stop -> Found.
            if (event instanceof InputEvent.Stop) {
                outbox.add(new OutputEvent.Flag());
                vehicle.raiseFlag();
                vehicle.move(0.0, Angle.Front);
                currentMode = MVMode.Found;
            }
            // CD-MV-Beh23: on Resume -> Waiting.
            else if (event instanceof InputEvent.Resume) {
                currentMode = MVMode.Waiting;
            }
            // CD-MV-Beh21: on Turn -> Going (entry: move(lv, a)).
            else if (event instanceof InputEvent.Turn) {
                InputEvent.Turn te = (InputEvent.Turn) event;
                a = te.a();
                vehicle.move(Constants.lv, a);
                currentMode = MVMode.Going;
            }
        } else if (currentMode == MVMode.Found) {
            // CD-MV-Beh9: autonomous transition to Final sink.
            currentMode = MVMode.Final;
        }
        // Final is the sink (j1) — no outgoing transitions.
    }
}
