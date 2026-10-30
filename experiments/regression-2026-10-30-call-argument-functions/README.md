# Regression: functions called inside operation-call arguments (30 October 2026)

Pre-registration: `review/prereg-v3-study-free.md`, Amendment 2 (paper repository,
committed before this change).

**Change.** `collectFunctionNamesFromAction` in `java2robochart.etl` now also
descends into every statement kind other than sequences and assignments (an
operation `Call`, a `Wait`, a `CommunicationStmt`) and collects function calls
from every expression they contain. Before, a function called inside an argument,
e.g. `actuator.op(V, sensor.f(x))`, was used in the RoboChart model but never
declared, and the CSP generator stopped on an unresolved reference.

**Method.** Each of the 308 Java sources the paper or letter reports was
extracted twice, by the published extractor and by the same extractor with only
this change, each phase (T2M, M2M, Dafny, RoboChart text, Isabelle) in its own
process through `forge.transformations.core.App`, as in the pipeline. The
generated RoboChart model, Dafny files and Isabelle theories were compared,
with set elements, `\<or>`-lists and declarations put in a fixed order (the
generator writes those in an order that varies between builds). As a check on
the method, the unfixed extraction of each of the twelve v3 sessions reproduces
its recorded artefacts exactly under the same comparison.

**Result** (`regression.tsv`, one row per source). Every source extracts with
both versions. 307 of 308 produce identical artefacts. The one that changes is
cold v3 Chemical Detector pass 3, by three added lines and nothing removed:
`function evasionAngle ( l : Loc ) : Angle { }` in the RoboChart model and
`consts evasionAngle :: " Loc \<Rightarrow> Angle"` in each of its two theories.

| Sources | Count | Changed |
|---|---|---|
| v3 sessions collected so far | 12 | 1 |
| cold-baseline-v2 | 15 | 0 |
| convergence-v3-frozen and reference-multicontroller-f (Table 7) | 26 | 0 |
| ablation-d2-diagnostic and multicontroller-e | 35 | 0 |
| reference runs | 3 | 0 |
| mutation campaign subjects | 186 | 0 |
| cold baseline (May) | 30 | 0 |
| Steam Boiler run | 1 | 0 |

**Tests.** New `M2mCallArgumentFunctionTest` (two cases): the function called
inside an operation-call argument is declared (fails on the unfixed extractor,
passes with the fix), and the same function assigned first is declared by both.
The existing model-to-model suites (`Java2RoboChartTransformTest` 35,
`M2mDataPlaneLiftRegressionTest` 11, `M2mSemanticPreservationRegressionTest` 8,
`TransformPhaseTest` 1) pass on both versions.
