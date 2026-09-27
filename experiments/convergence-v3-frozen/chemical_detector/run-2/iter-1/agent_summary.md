# chemical_detector — run 2, iteration 1 (cold codegen)

## What I did

Wrote 16 Java files under `src/main/java/chemical_detector/` from the
specification alone. Nothing pre-existed, so nothing was deleted;
`build.gradle` untouched. Two state machines, matching CD-ARCH2's two
reasoning subsystems: `GasAnalysisController` (Reading, Analysis, NoGas,
GasDetected, Stopped) and `MovementController` (Waiting, Going, Found,
Avoiding, TryingAgain, AvoidingAgain, GettingOut). One `SystemEvent` sealed
interface carries every event, so turn/stop/resume exist exactly once and
extract as shared events. `gradlew build` clean; all 81 requirements traced
in `result_codegen.json`, every file covered.

## Decisions the requirements did not settle

- **Chem** (CD-DM4) is opaque but CD-Fn1 needs a target species: modelled as
  `enum Chem { NoChem, TargetChem }`.
- **Intensity** → `double`. `goreq` (CD-Fn4) is used inside `intensity()`,
  but the threshold guard is plain `ins >= thr`: a two-argument extracted
  function in a guard risks the CSP generator's unsupported-callee path.
- **Constants** (CD-Const1..6) unspecified; all set to 1 — integral and
  inside the 0..1 CSP ranges, so nothing is ceiled and no guard is trivially
  false in the checked model.
- **CD-OP1..4** are `Vehicle` methods, not `operation/` classes with
  `compute()`: they are platform operations, not derived computations.
- **CD-Evt3 (odometer)** is a zero-argument sensor query, not a consumed
  event, because the requirement describes sampling.
- **Terminal modes.** No requirement asks for `Tick`, but the codegen rules
  demand an outgoing transition per mode, so `Stopped` and `Found` carry
  Tick self-loops, and the gas-analysis terminal mode is named `Stopped`,
  not `Final`. Instrument accommodations, deviating from CD-DC1.
- Modes whose autonomous guards are jointly exhaustive (Analysis,
  GasDetected, AvoidingAgain) use a total guard cover, no self-loop.

## Unresolved

CD-MV-FR1 wants `randomWalk()` as Waiting's *during* action; `step()` has no
during form, so it is emitted as an entry action.
