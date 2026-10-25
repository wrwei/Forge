# chemical_detector run 3 — iteration 1 (cold codegen)

**Changed.** Wrote a new tree under `java.generated.project/src/main/java/chemical_detector/` (20 files; nothing pre-existed, nothing deleted) plus `result_codegen.json` (116 rows, all 81 requirements, every file traced). Two mode-nested controllers: `GasAnalysisController` (Reading, Analysis, NoGas, GasDetected, Concluded) and `MovementController` (Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut). GA emits `turn`/`stop`/`resume` as `OutputEvent`s that MV consumes as same-named `InputEvent`s; MV emits `flag`. Functions `analysis`/`intensity`/`location`/`goreq` and `odometer()` live on the `@SensorService` `ChemSensor`; `move`/`randomWalk`/`shortRandomWalk`/`pause` are on `Vehicle`; `changeDirection(l)` is an operation class. Clock `evasionStart` is reset on entry to Avoiding; stuck test is `nowMs() - evasionStart < STUCK_PERIOD || d1 - d0 > STUCK_DIST`.

**Decisions the spec did not settle.**
- No `Final` mode: GA ends in `Concluded`, MV stays in `Found`; both self-loop on a `Tick` input so every mode has an outgoing transition. The flag/halt actions sit on the incoming `stop` transitions, so the self-loop does not re-emit `flag`.
- Complementary guards written as `p` / `!p` (total covers).
- Odometer is a sensor reading, not an input event.
- Invented values: THR=3.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. `Chem` = enum {TARGET, OTHER}. `analysis` = some value with `c == TARGET` and `i > 0`. `angle(x)` maps 1-based positions to Left, Right, Back, Front cyclically.

**Unresolved / for review.**
- `intensity` takes the maximum over all chemicals, as the spec literally says.
- AvoidingAgain has no `turn` transition, so a turn arriving there is dropped.
- I checked syntax with plain `javac` into the scratchpad (not a pipeline phase). I wrote no unit tests.
- An additional working directory attached to this session points at another study's LRE Java tree. I did not read it.
