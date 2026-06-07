# Post-Codegen Review — chemical_detector (compile-only ablation, iter-1)

**Status:** uncertain

Cold codegen of the chemical_detector study from
`requirement_all.json` + `system_description.txt`. All 81 requirements
implemented across 16 files in package `chemdetector`
(two step()-controllers: `GasAnalysisController`, `MovementController`;
dispatcher facade `ChemicalDetector` without a mode field).

## Issues

### 1. invented_default — Constant values (CD-Const1..6)
THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1.0,
OUT_PERIOD=1 chosen arbitrarily (small values keep the FDR4 state space
small). TARGET_CHEM=1 invented entirely — no requirement names the
target chemical's identity.
→ Confirm or supply the configured startup values.

### 2. design_choice — Chem/Intensity as annotated primitives (CD-DM4, CD-DM5)
Chem is `int @RoboChartType("nat")`; Intensity is
`double @RoboChartType("real")`. Wrapper records were rejected:
guard comparisons (`ins >= thr`), the Constants interface, and
function extraction only work over primitives; multi-arg `goreq(a,b)`
is not extractable in guard position, so `goreq` is a static helper
used inside `intensity()` and the threshold check is the direct
comparison `ins >= CdConstants.THR`.

### 3. design_choice — Live `Concluded` mode instead of a GA Final state (CD-GA-Beh6)
A RoboChart Final state would make the generated Isabelle theory's
deadlock-freedom goal unprovable (CLAUDE.md: theory-generated
controller must not contain Final). `GasDetected --[ins >= thr]-->
Concluded` emits `stop`; `Concluded` consumes further gas events in a
guard-free self-loop and discards them ("processing no further
readings" preserved in effect).

### 4. invented_default — analysis() classifier semantics (CD-Fn1)
"Reading indicates the target chemical" implemented as: any sample with
`c == TARGET_CHEM && i > 0.0` → gasD. Empty readings: noGas,
intensity 0.0, location Front (safe defaults — no sentinel checks in
the controller).

### 5. invented_default — Sensor index → Angle mapping (CD-Fn3)
`angle(x)` undefined in the spec; implemented as `idx % 4` →
Front, Left, Back, Right.

### 6. design_choice — during-action realisation (CD-MV-FR1, CD-MV-Beh17)
`randomWalk()` (a during action in the spec) sits at the head of the
Waiting mode block (pipeline supports entry actions only; re-issued
each cycle in Waiting, operationally equivalent). Entry actions of
Found/Avoiding/TryingAgain/GettingOut/Going are inlined per incoming
transition; the AvoidingAgain→Avoiding return path re-runs the full
Avoiding entry (d0 sample, timer restart, changeDirection, evadeTime
wait) per CD-MV-Beh17 + CD-MV-Var2.

### 7. design_choice — Liveness self-loops; relay; changeDirection arity
(a) `Found` self-loops on `stop`, `Concluded` self-loops on `gas` —
every state keeps a bare-precondition operation without τ-self-loops.
(b) turn/stop/resume are `MovementEvent` records emitted via
`AnalysisRelay.apply(new MovementEvent.X(...))` (CD-ARCH2 channel).
(c) `changeDirection(velocity, side)` is 2-arg although CD-OP4 writes
`changeDirection(l)` — the velocity constant is explicit so the call
extracts as an operation call, not a single-payload event.

## Next step
Run compile → coverage → preflight → t2m → m2m → m2t → dafny_gen →
isabelle_gen and iterate on visible feedback (compile-only ablation:
the four behavioural verifiers are withheld).
