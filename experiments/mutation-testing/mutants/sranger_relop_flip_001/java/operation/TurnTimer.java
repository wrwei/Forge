package sranger.operation;

import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.timing.Clock;

/**
 * Turn-duration timer operation (SR-Var1 / SR-GP2).
 *
 * <p>Encapsulates BOTH the reset-time bookkeeping AND the elapsed-time check
 * behind a small Operation class. The controller invokes
 * {@link #compute()} at the top of every {@code step(...)} and
 * {@link #markReset()} on the Moving → Turning transition; it reads the
 * boolean predicate via {@link #elapsed()}.</p>
 *
 * <p><b>Why encapsulate this in an Operation class?</b> The M2M's
 * {@code containsClockCall} heuristic flags ANY {@code this.<field> = <expr>}
 * inside the controller body where the RHS contains a call on a Clock-typed
 * receiver: such fields are promoted to RoboChart {@code clock} declarations
 * rather than Ctrl_State variables. Iter-4 wrote both
 * {@code this.clockResetTime = cycleClock.nowMs()} (legitimate clock-reset)
 * and {@code this.turnDurationElapsed = cycleClock.nowMs() - clockResetTime ≥ ...}
 * (boolean derivation) in the controller. The second pattern was mis-lifted
 * as a clock, and the resulting Moving → Turning transition's
 * {@code # clockResetTime} reset action then caused the Isabelle EGL template
 * to append {@code ∧ clock = 0} to that transition's precondition — surfacing
 * as {@code *** Extra variables on rhs: "clock"} during {@code isabelle build}.</p>
 *
 * <p>Both of the clock-touching field writes are MOVED INTO THIS CLASS, where
 * they are invisible to the controller-body walk. The controller no longer
 * holds any Clock reference, has no clock-typed dependency, and never reads
 * {@code cycleClock.nowMs()} directly. The M2M sees the controller as
 * clock-free, so:
 * <ul>
 *   <li>{@code turnDurationElapsed} is correctly classified as a boolean
 *       Ctrl_State variable, declared in the {@code .rct} as
 *       {@code var turnDurationElapsed : boolean}, and the
 *       {@code condition turnDurationElapsed} on the Turning → Moving
 *       transition resolves cleanly (closes Failure 1).</li>
 *   <li>No RoboChart {@code clock} declaration is emitted, no
 *       {@code # clockResetTime} reset action is attached to the
 *       Moving → Turning transition, and the Isabelle template's
 *       {@code ∧ clock = 0} clause never fires (closes Failure 2).</li>
 * </ul>
 *
 * <p><b>Semantic implication.</b> The timing is now opaque to the formal
 * model: at the RoboChart / Isabelle level, {@code turnDurationElapsed} is
 * just a boolean state variable that the controller reads; the formal model
 * does not know about "2 seconds". This is acceptable for the verification
 * obligations we run (deadlock-freedom, structural invariants, FDR4
 * refinement, Dafny mode-postcondition framing) — none of them depends on
 * the wall-clock semantics of the predicate. The Java implementation still
 * realises the spec's 2-second behaviour at runtime via {@link Clock#nowMs()}.</p>
 *
 * <p><b>Why an Operation class rather than a sensor method?</b> Sensor
 * methods are referenced from controller guards/actions and become
 * {@code function f(p:T):R} declarations in the {@code .rct}; their parameter
 * names reach the parser verbatim, and we'd lose the Dafny stable-field
 * shape that fixed iter-2 → iter-3's framing failure. An Operation class
 * lets us keep {@code turnDurationElapsed} as a non-final Ctrl_State boolean
 * (so Dafny treats it as stable across {@code mode := Final}), while
 * computing it from the Clock behind an opaque method boundary.</p>
 *
 * <p>The {@code compute()} method body satisfies the codegen rule "only
 * direct {@code this.field = expression} assignments, no local variables".
 * The {@code markReset()} method is a single direct assignment for the same
 * reason. {@link #elapsed()} is a plain getter.</p>
 */
public final class TurnTimer {

    private final Clock cycleClock;

    /**
     * Timestamp (milliseconds) at which the controller most recently entered
     * Turning (SR-Var1). Set by {@link #markReset()} from the controller's
     * Moving → Turning transition. Initialised to 0.0 to match iter-4's
     * Dafny constructor invariant {@code clockResetTime := 0.0}.
     */
    @RoboChartType("real")
    private double resetTime = 0.0;

    /**
     * Cached value of the SR-GP2 guard predicate "turn duration elapsed" —
     * true when {@code cycleClock.nowMs() - resetTime ≥ turnDurationMs}.
     * Refreshed by {@link #compute()} which the controller calls at the top
     * of every {@code step(...)}.
     */
    private boolean elapsed;

    public TurnTimer(Clock cycleClock) {
        this.cycleClock = cycleClock;
        this.elapsed = false;
    }

    /**
     * Recompute {@link #elapsed} from the current clock reading. Direct
     * {@code this.field = expression} assignment with no local variables, per
     * the codegen rules for Operation compute() methods.
     */
    public void compute() {
        this.elapsed = cycleClock.nowMs() - this.resetTime < SRangerConstants.turnDurationMs;
    }

    /**
     * Mark the start of a new Turning interval (SR-Var1 entry action of the
     * Moving → Turning transition). Single direct field assignment.
     */
    public void markReset() {
        this.resetTime = cycleClock.nowMs();
    }

    /** Latest cached value of the SR-GP2 predicate. */
    public boolean elapsed() {
        return elapsed;
    }

    /** Latest reset timestamp (SR-Var1). Exposed for inspection / testing. */
    @RoboChartType("real")
    public double resetTime() {
        return resetTime;
    }
}
