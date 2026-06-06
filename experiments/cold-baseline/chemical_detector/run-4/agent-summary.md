# Chemical Detector — Cold Baseline Run 4

Single-shot implementation produced from the system description and `requirement_all.json` only; no prior session state or verifier feedback consulted.

## Architecture

- Two controllers: `GasAnalysis` (Reading -> Analysis -> {NoGas, GasDetected} -> Final) and `Movement` (Waiting, Going, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Found, Final).
- Shared events (`turn`, `stop`, `resume`) flow GasAnalysis -> Movement via `Movement.receive(OutputEvent)`. The Vehicle receives `flag` and motion commands. The Clock backs the movement subsystem's `T` clock for stuck detection.
- `GasSensorService` provides the analysis / intensity / location / goreq / angle functions (CD-Fn1..4). `ChemDetectorConstants` provides `thr`, `lv`, `evadeTime`, `stuckPeriod`, `stuckDist`, `outPeriod`.

## Codegen-rules choices

- Each `step()` advances at most one transition; the outer if-else is pure mode-dispatch, the inner chain orders triggers (stop > turn > obstacle > resume > autonomous), and entry actions are inlined.
- `changeDirection(l)` (CD-OP4) is inlined as an if/else dispatch inside the Avoiding transition action rather than a `compute()` operation, because its body is side-effecting (calls `Vehicle.move`) and does not match the `this.field = expr` shape required for operation classes.
- Named boolean predicates encode the stuck-detection guards using simple arithmetic (no ternary, no sentinel existence checks).
- Bare-precondition cover for `deadlock_free` is provided by event-triggered branches; no τ-self-loop fallbacks.

## Invented defaults

Constant values (`thr=10.0`, `lv=1.0`, `evadeTime=2`, `stuckPeriod=5`, `stuckDist=1.0`, `outPeriod=3`) and the `Chem`/`Intensity` representations (`int id`, `double value`) are invented placeholders — the requirements declare these as opaque or configurable. `Main` is a smoke-test demo only.
