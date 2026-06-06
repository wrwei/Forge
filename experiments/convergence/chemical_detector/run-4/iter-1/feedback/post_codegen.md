# Post-Codegen Review — chemical_detector, iter 1 (cold codegen)

**Status:** complete-with-notes

**Summary:** Full Java tree generated under `java.generated.project/src/main/java/chemdetector/`
from `requirement_all.json` + system description: 17 files — two controllers
(GasAnalysisController, MovementController), sealed InputEvent/OutputEvent
hierarchies, VehicleSensors (analysis/intensity/location/goreq/odometer),
Vehicle actuation surface, Clock, Constants, four domain enums + GasSensor
record, @RoboChartType annotation. All 88 requirement ids traced in
`result_codegen.json`.

## Issues for user review

1. **design_choice — no Final states in either state machine.**
   CD-GA-Beh6 and CD-MV-Beh9 specify transitions to final state j1. CLAUDE.md
   states the theory-generated controller must NOT contain a Final state (the
   Isabelle deadlock_free closer hangs unprovably). Implemented instead:
   GasAnalysis gains a live `Done` mode (absorbs further `gas` readings);
   Movement's `Found` self-loops on `stop`. The terminating events (`stop`,
   `flag`) are still emitted on the way in, so observable behaviour up to
   termination matches the spec.

2. **design_choice — odometer is a sensor read, not an event** (CD-Evt3,
   CD-MV-FR4, CD-MV-Beh16). The spec's `odometer ? d0` mid-entry reception is
   not expressible in the single-step Java idiom; implemented as
   `d0 = sensors.odometer()` (zero-arg sensor method → RoboChart Sensors
   interface variable).

3. **design_choice — CD-OP1..4 are Vehicle methods, not compute() operation
   classes.** The spec marks them "provided by the Vehicle"; the compute()
   operation-class pattern is for controller-side computations, of which this
   study has none. Note: the M2M models >=2-arg calls (`move`) as operation
   Calls and 0/1-arg calls (`randomWalk`, `shortRandomWalk`,
   `changeDirection`) as output-event communications.

4. **invented_default — Chem literals and target chemical.** Chem is opaque in
   the spec; implemented as `enum Chem { CH4, CO2 }` with the target injected
   into VehicleSensors' constructor. `analysis` classifies gasD iff any reading's
   chemical equals the target (presence at any intensity).

5. **invented_default — Intensity = double/real.** The spec's totally-ordered
   opaque Intensity type is encoded as double with `goreq` as the comparison.
   `intensity()` returns 0.0 for an empty reading (safe default per the
   sensor-layer rule).

6. **invented_default — all constants = 1** (THR, LV, EVADE_TIME, STUCK_PERIOD,
   STUCK_DIST, OUT_PERIOD) so values fit the configured FDR4 type ranges {0..1}.

7. **design_choice — Waiting's during-action `randomWalk()`** (CD-MV-FR1) is
   encoded as a top-of-mode statement, which the M2M extracts as a state ENTRY
   action (RoboChart `during` is not producible by this pipeline).

8. **invented_default — angle() index mapping.** Spec leaves the index→direction
   map abstract (1-based); implemented 0-based: 0=Front, 1=Left, 2=Right,
   else Back.

9. **design_choice — total guard covers on autonomous-only modes.** Analysis
   (sts two-valued), GasDetected (goreq vs !goreq), AvoidingAgain (stuck
   disjunction vs negation) have jointly-exhaustive guards and no self-loops,
   per CLAUDE.md's preferred pattern for deadlock-freedom without τ/Tick
   self-loops. The M2M deadlock-lint may still flag these states (documented
   as too broad).

**Next step:** run the deterministic pipeline (`run_experiment_iteration.py`).
