# Final-mode divergence between the CSP and Isabelle chains (Referee 1, point M2)

This folder holds the artefacts behind the Chemical Detector `Final`-mode example
in Section 4.4 (RQ2) and the reply to Referee 1's second major point. They come
from one iteration of a convergence run, recorded before the extractor marked
terminal states. It is the only recorded iteration in which `Final` is an empty
mode block with no outgoing operation, so it shows the two encodings side by side.

Iteration 2 of the same run added a `Tick` self-loop to `Final`, after which both
verifiers passed. The convergence runs published in `experiments/convergence`
are a later set; none of them contains an empty `Final`.

## Files

| File | What it is |
| ---- | ---------- |
| `java/` | the Java the pipeline extracted from |
| `robochart_controller.rct` | the extracted RoboChart model (CSP chain, Phase 5b input) |
| `csp/timed/` | the CSP the RoboChart generator produced from it |
| `GasAnalysisController_Beh.thy` | the extracted Z-Machine theory (Isabelle chain, Phase 5c) |
| `fdr4_verdict.md` | the FDR4 phase report: 9 assertions checked, 9 passed |
| `isabelle_verdict.md` | the Isabelle phase report: `GasAnalysisController_deadlock_free` timed out |

## What the two chains do with `Final`

**CSP chain.** In `robochart_controller.rct`, `Final` is a state in both
controllers with no outgoing transition. The generated CSP gives every state the
same during-behaviour, `(SKIP ; SStop) /\ interrupt -> SKIP`. In the timed
semantics the generator uses, `SStop` is defined in `defs/state_timed_defs.csp` as
`CRUN({|share__|})`, with `CRUN(x) = RUN(union(x, {tock}))`. A state with no
outgoing transition therefore keeps offering `tock`: time can pass, so the
process is not deadlocked, and FDR4's deadlock-freedom assertions hold. Every
`*_coreassertions.csp` file includes `state_timed_defs.csp`; none includes the
untimed `state_defs.csp`.

**Isabelle chain.** In `GasAnalysisController_Beh.thy`, `Final` is a value of the
state type `St`, but none of the seven operations has `Final` in its
precondition, and the store invariant is `tr ≠ []`, which cannot mark a state as
terminal. The deadlock-freedom lemma requires some operation to be enabled in
every reachable state, including `Final`, where none is, so it cannot be
discharged; the phase report records the proof timing out.

The two verdicts therefore concern two different encodings of the same Java,
not a difference between refinement checking and theorem proving.
