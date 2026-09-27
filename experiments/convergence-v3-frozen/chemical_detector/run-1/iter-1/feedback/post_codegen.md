# post_codegen — chemical_detector, iteration 1

status: uncertain

## Summary

81/81 requirements implemented across 17 Java files in
`java.generated.project/src/main/java/chemical_detector/`. Two controllers
(`GasAnalysisController`, `MovementController`) realise the two reasoning
subsystems of CD-ARCH2; they communicate only through `MovementBus`
(turn / stop / resume). `result_codegen.json` traces every requirement id and
names every file.

## Issues

### 1. invented_default — Chem modelled as a two-literal enumeration
requirement_ids: CD-DM4, CD-Fn1
CD-DM4 calls `Chem` an *opaque* type needing only equality. Java needs a
concrete type and `analysis` needs a concrete classification rule, so `Chem`
is `enum Chem { Target, Other }` and `analysis` returns `gasD` iff some sensor
in the reading carries `Chem.Target`. Nothing in the requirements names the
target species.
fix_directive: If the target chemical should be configuration rather than a
type literal, add a `Chem` constant to `ChemicalDetectorConstants` and compare
against it in `GasAnalysisFunctions.analysis`.

### 2. design_choice — Intensity modelled as `double`
requirement_ids: CD-DM5, CD-Const1, CD-Fn2, CD-Fn4
`Intensity` is specified only as totally ordered. A dedicated record would be
retyped to RoboChart `real` by the extractor anyway (and would be mistaken for
the sensor record by the M2M's dependency-chain heuristic), so intensities are
plain `double` and `goreq` is `i1 >= i2`. `thr` is a `double` constant.

### 3. invented_default — sensor position to Angle mapping
requirement_ids: CD-Fn3, CD-DM7
CD-DM7 says the 1-based position in a reading "maps to a sensing direction"
but never gives the map. `GasAnalysisFunctions.angle` uses
1→Left, 2→Right, 3→Back, otherwise Front.

### 4. design_choice — gas-analysis terminal mode is `Stopped`, not a Final state
requirement_ids: CD-GA-Beh6, CD-GA-FR4
CD-GA-Beh6 ends the gas-analysis subsystem in a final state. A RoboChart
`Final` state in the theory-generated machine leaves the deadlock-freedom
obligation unprovable (CLAUDE.md, "Interpreting Isabelle Results"), so the
subsystem instead enters a live `Stopped` mode whose only transition is a
`gas`-triggered self-loop: it still emits `stop` once, and it processes no
further readings (it stores the reading and re-enters `Stopped`). This gives
`Stopped` a bare-precondition operation without an autonomous self-loop.

### 5. design_choice — complementary guards instead of enum-literal guards
requirement_ids: CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh7, CD-MV-Beh17, CD-MV-Beh18
`Analysis`, `GasDetected` and `AvoidingAgain` are left by pairs of autonomous
transitions written as `p` / `!p` (`statusNoGas`, `insAtOrAboveThr`,
`makingProgress`). This is a total guard cover, so those modes are
deadlock-free with no self-loop and the residual Isabelle goal is `p or not p`.
Writing the second guard as `sts == gasD` instead would need enum exhaustion
inside the proof. Semantically identical because `Status` has exactly two
values (CD-DM1).

### 6. invented_default — constant values
requirement_ids: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6
No values are given. Integral values were chosen (`THR=5.0`, `LV=1.0`,
`EVADE_TIME=2`, `STUCK_PERIOD=10`, `STUCK_DIST=1.0`, `OUT_PERIOD=3`) because
the RoboChart CSP generator ceils fractional constants.

### 7. design_choice — odometer is a sensor read, not an event
requirement_ids: CD-Evt3, CD-MV-Var2, CD-MV-Var3
CD-Evt3 says the movement subsystem *samples* the distance into d0/d1. It is
therefore `Vehicle.odometer()`, a zero-argument sensor method read from
transition actions (it surfaces in the RoboChart `Sensors` interface), rather
than a fourth `MovementEvent`.

### 8. ambiguous_requirement — clock name
requirement_ids: CD-MV-Clock1
The README calls the movement clock `T`; the requirement text names no
identifier. The Java field is `evadeStart` (assigned from `Clock.nowMs()`), so
the extracted RoboChart clock is `evadeStart` and the stuck guard becomes
`since(evadeStart) < stuckPeriod`.

### 9. scope_question — `Resume` is not accepted in `Found`
requirement_ids: CD-MV-FR3, CD-MV-Beh9
CD-MV-FR3 says the movement subsystem "remains stopped" once in `Found`, and
no CD-MV-Beh requirement gives `Found` a resume transition, so `Found` accepts
only `stop` (a self-loop, which is what CD-MV-Beh9 is mapped to). This is safe
because the gas-analysis subsystem emits nothing after `stop`.
