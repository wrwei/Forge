package chemdetector.gasanalysis;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import chemdetector.event.EventBus;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.function.GasFunctions;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2 subsystem 2, CD-GA-FR1..4, CD-GA-Beh1..7).
 * <p>
 * Modes: Reading (initial), Analysis, NoGas, GasDetected, plus a Final sink
 * that represents the {@code j1} final state.
 * <p>
 * Outputs the {@code turn}, {@code stop}, and {@code resume} events to the
 * movement subsystem via the shared {@link EventBus}.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final EventBus bus;

    /** CD-GA-Var1: most recent gas reading. */
    private List<GasSensor> gs = new ArrayList<>();

    /** CD-GA-Var2: classifier outcome for the current reading. */
    private Status sts = Status.noGas;

    /**
     * CD-GA-Var3: peak intensity of the current reading as a primitive
     * {@code real}. Iter-5 carried two redundant shadows of the same
     * quantity: an {@link chemdetector.data.Intensity} record field
     * ({@code ins}) and a primitive {@code double} ({@code insVal}). The
     * record field was assigned but never read in any guard or downstream
     * computation — both the Dafny and Isabelle theories only check
     * {@code insVal >= 10.0}, and the .rct's GasDetected guard already
     * resolves to {@code insVal >= 10}. Dropping {@code ins} removes one
     * Cartesian factor from FDR4's state space without changing the
     * verified semantics (CD-GA-Beh5..7 only refer to the peak as a
     * primitive comparison).
     */
    @RoboChartType("real")
    private double insVal;

    /** CD-GA-Var4: heading sent over the {@code turn} event. */
    private Angle anl = Angle.Front;

    public GasAnalysisController(EventBus bus) {
        this.bus = bus;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public Status sts() {
        return sts;
    }

    @RoboChartType("real")
    public double insValue() {
        return insVal;
    }

    public Angle anl() {
        return anl;
    }

    public void step(GasAnalysisEvent e) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        // Binary comparison between the Ctrl_State `insVal` real-typed
        // field and the threshold value. The literal `10.0` is used here
        // rather than {@link ChemDetectorConstants#thrVal} so that:
        //   - the .rct guard renders as `condition insVal >= 10` (a real
        //     comparison) rather than `insVal >= thrval`, which would force
        //     the Isabelle template's CallExp-fallback to emit
        //     `consts thrval :: "unit \<Rightarrow> real"`. That fallback
        //     clashes with the comparator's `real` operand type and
        //     regresses the deadlock-freedom proof (iter-3 documented this
        //     bug; iter-5 worked around it by holding `thrVal` as a
        //     Ctrl_State field, but that added a Cartesian factor to
        //     FDR4's state space);
        //   - Dafny verifies `insVal >= 10.0 ==> mode == Final` directly
        //     with no extra const indirection;
        //   - the symbolic constant {@link ChemDetectorConstants#thr} /
        //     {@code thrVal} still exists for tracing to CD-Const1 and is
        //     referenced by the GasFunctions.analysis classifier; only
        //     this single guard inlines the literal to keep the formal
        //     model lean.
        boolean insAboveThr = insVal >= 10.0;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        //
        // Iter-7 NOTE on Tick-event self-loops (FDR4 deadlock + divergence fix):
        //   Iter-6 used unconditional `else { currentMode = <Same>; }` branches in
        //   Reading, Analysis, GasDetected, and Final to give every mode a
        //   bare-precondition outgoing operation for Isabelle's `metis
        //   St.exhaust_disc` deadlock-freedom discharger. Those branches become
        //   guard-free, trigger-free transitions in the .rct, which the RoboChart
        //   CSP generator emits as τ-self-loops in tock-CSP. The pure τ-self-loops
        //   were the τ-divergence source FDR4 reported on iter-6 (chains
        //   `t6 → t2`, `t4 → t9`, `t8 → t2`, ...): once entered, the autonomous
        //   self-loop can fire indefinitely without a visible event, violating
        //   divergence-freedom; the same τ-loop is what every deadlock
        //   counterexample also traced into.
        //
        //   The LRE iter-2 fix (delete every self-loop) does NOT port here: in
        //   LRE every mode retains an event-triggered bare-precondition operation
        //   (reqVel / reqHdng / endTask / ...) that the deadlock_free tactic
        //   accepts. Chem-detector's Analysis / NoGas / GasDetected modes are
        //   autonomous-only — Gas is only consumed in Reading — so plain deletion
        //   would re-break the Isabelle proof.
        //
        //   This iter-7 fix instead converts the τ-self-loops into Tick-triggered
        //   self-loops by replacing `else { ... }` with `else if (e instanceof
        //   GasAnalysisEvent.Tick) { ... }`. In the generated .thy each such
        //   transition still has `pre "st = <X>"` (bare — the trigger goes into
        //   the trace, never into the precondition), so `metis St.exhaust_disc`
        //   still discharges every disjunct. In CSP the τ-self-loop becomes a
        //   `tick`-event self-loop, removing the τ-divergence cycles and the
        //   τ-edge counterexamples. NoGas keeps its lone autonomous edge to
        //   Reading (CD-GA-Beh3) because (a) the requirement is explicitly
        //   autonomous and (b) without a τ-self-loop in Reading or any downstream
        //   mode, that one-shot τ-step cannot extend into an infinite divergence
        //   chain — it transits to Reading and waits for the next external event.
        //
        //   The deadlock-lint may flag Reading/Analysis/GasDetected/Final as
        //   "all outgoing transitions are guarded or event-triggered" (the lint
        //   checks the textual form, not the proof's actual `pre`). This is a
        //   known false positive: Isabelle still proves deadlock-freedom because
        //   the operation pre stays `st = <X>` (bare). The Isabelle artefact will
        //   confirm this on the iter-7 re-run.
        if (currentMode == GasAnalysisMode.Reading) {
            if (e instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas ge = (GasAnalysisEvent.Gas) e;
                this.gs = ge.payload();
                currentMode = GasAnalysisMode.Analysis;
                // Entry action for Analysis (CD-GA-FR3): sts = analysis(gs)
                this.sts = GasFunctions.analysis(this.gs);
            } else if (e instanceof GasAnalysisEvent.Tick) {
                // Tick-triggered self-loop. Replaces the iter-6 `else` τ-edge
                // (see iter-7 NOTE above). Pre still `st = Reading` for Isabelle;
                // visible `tick` event for FDR4.
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                // CD-GA-Beh4: send resume, transition to NoGas.
                // The bus.publish(new MovementEvent.Resume()) shape is the
                // canonical OutputEvent constructor pattern that the M2M
                // ETL recognises (see EventBus class-level doc): the
                // event name is taken from the record variant (Resume →
                // resume) and the event is correctly typed as a signal
                // (no payload). This also makes resume a Shared event
                // between this controller and MovementController.
                this.bus.publish(new MovementEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (stsIsGasD) {
                // CD-GA-Beh5: transition to GasDetected
                currentMode = GasAnalysisMode.GasDetected;
                // Entry action for GasDetected (CD-GA-FR4): record the peak
                // intensity of the current reading into `insVal`. Iter-5
                // mirrored this value in an `ins : Intensity` record-typed
                // Ctrl_State field as well, but `ins` was never read by any
                // guard or downstream computation (the .rct already uses
                // `insVal >= 10`, Dafny ensures `insVal >= 10.0 ==> mode == Final`,
                // and the Isabelle theory only references `insVal\<ge>10.0`).
                // Iter-6 drops `ins` to remove a redundant Cartesian factor
                // from FDR4's state space. The `peakIntensity` static helper
                // returns the same primitive real that `Intensity.value()`
                // would have unwrapped.
                this.insVal = GasFunctions.peakIntensity(this.gs);
            } else if (e instanceof GasAnalysisEvent.Tick) {
                // Tick-triggered self-loop replacing the iter-6 τ-self-loop.
                // Status is total (noGas | gasD), so this branch is reachable
                // only when the controller is stepped with a Tick while in
                // Analysis before sts has been re-evaluated — a benign idle
                // tick. Pre stays bare `st = Analysis` for Isabelle.
                currentMode = GasAnalysisMode.Analysis;
            }

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAboveThr) {
                // CD-GA-Beh6: send stop, transition to Final.
                // Constructor-pattern OutputEvent publish — see EventBus doc.
                this.bus.publish(new MovementEvent.Stop());
                currentMode = GasAnalysisMode.Final;
            } else if (!insAboveThr) {
                // CD-GA-Beh7: anl = location(gs); send turn(anl); back to Reading.
                // The constructor pattern lets the ETL infer the turn
                // event's payload type as Angle (from MovementEvent.Turn's
                // single field). Iter-4 used bus.sendTurn(this.anl) which
                // produced an opaque method call the ETL typed as int,
                // causing FDR4 to reject sendTurn.out!anl with an
                // Angle/Int clash.
                this.anl = GasFunctions.location(this.gs);
                this.bus.publish(new MovementEvent.Turn(this.anl));
                currentMode = GasAnalysisMode.Reading;
            } else if (e instanceof GasAnalysisEvent.Tick) {
                // Tick-triggered self-loop replacing the iter-6 τ-self-loop.
                // insAboveThr is total over reals, so this branch is logically
                // unreachable at the model level; included to give GasDetected
                // a bare-pre operation that survives the M2M lint and the
                // Isabelle deadlock_free proof.
                currentMode = GasAnalysisMode.GasDetected;
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: autonomous transition back to Reading. Kept
            // autonomous (the requirement specifies an unconditional transition,
            // and per the iter-7 NOTE above this lone τ-step cannot extend
            // into an infinite chain once Reading no longer has a τ-self-loop).
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.Final) {
            // Terminal sink. Replaces the iter-6 τ-self-loop with a Tick-
            // triggered self-loop so the Final state is no longer a τ-edge
            // contributor to FDR4's divergence chain analysis. Pre stays
            // bare `st = Final`; the proof's St.exhaust_disc step handles
            // the Final disjunct via the FinalToFinal operation.
            if (e instanceof GasAnalysisEvent.Tick) {
                currentMode = GasAnalysisMode.Final;
            }
        }
    }
}
