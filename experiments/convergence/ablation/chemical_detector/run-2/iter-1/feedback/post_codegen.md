# Phase 2 — Interactive Code Generation (iter-1, cold) — uncertain

All 60 requirements implemented across 16 Java files (2 controllers:
GasAnalysisController, MovementController). 8 issues flagged below.

## Issues

### 1. design_choice — CD-GA-Beh6: GasDetected found-branch reroutes to Reading instead of a Final state
The requirement says the gas-analysis subsystem "concludes its search,
processing no further readings" after emitting stop. A RoboChart Final
state on a theory-generated controller makes the Isabelle
deadlock-freedom goal unprovable (CLAUDE.md "Interpreting Isabelle
Results"), so the stop branch emits the stop event and reroutes to
Reading. The movement subsystem is in Found and ignores further
turn/resume events, so the deviation is not observable at the system
boundary.
*Fix directive:* If a literal terminal state is required, re-introduce a
Final mode and accept the Isabelle limitation, or confirm the reroute.

### 2. design_choice — CD-MV-Beh9: Found state given an event-triggered stop self-loop
"Once in Found the movement subsystem stays halted" — implemented as an
explicit self-loop on the stop event so Found has a bare-precondition
operation (deadlock cover) without a τ-self-loop (divergence) or an
unconditional else (determinism).

### 3. design_choice — CD-OP4: changeDirection implemented on Vehicle rather than an operation class
changeDirection(l) takes a parameter and commands motion; the
operation-class compute() pattern (no-arg, field-assignments-only)
cannot express it, so it lives on Vehicle next to move.

### 4. design_choice — CD-DM7/CD-Evt4..6: reading type and shared-event encoding
GasSensorReading is List<GasSensor> directly (extracts to
Seq(GasSensor)); turn/stop/resume are records of the same simple name in
both InputEvent (trigger side) and OutputEvent (action side) so the ETL
can pair them into the Shared interface.

### 5. invented_default — Constant values (CD-Const1..6)
thr=1.0, lv=1.0, evadeTime=2, stuckPeriod=5, stuckDist=1.0, outPeriod=3.
Small integers keep the CSP state space tractable.

### 6. invented_default — Chem species, target identity, direction mapping
Chem = {chemA, chemB}, target = chemA. Direction map: index 0→Front,
1→Left, 2→Right, ≥3→Back (CD-DM7 says 1-based positions map to sensing
directions but gives no layout).

### 7. invented_default — Empty-reading and detection defaults
analysis(∅)→noGas, intensity(∅)→0.0, location(∅)→Front; "indicates the
target chemical" realised as c == target && i > 0.0.

### 8. ambiguous_requirement — Transition priority within movement modes
Implemented order in every mode block: stop > turn > obstacle > resume >
autonomous guards. CD-DC1 holds either way.

## Next step
Run the deterministic pipeline (compile → coverage → preflight → t2m →
m2m → m2t → dafny_gen → isabelle_gen) and iterate on visible feedback.
