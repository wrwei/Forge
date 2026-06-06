# Chemical Detector — Cold Baseline Run 7

Single-shot generation. No prior session, memory, or verifier feedback.

## Approach

Two-controller decomposition matching CD-ARCH2:

- `GasAnalysisController` — Reading / Analysis / NoGas / GasDetected / Final.
- `MovementController` — Waiting / Going / Avoiding / TryingAgain / AvoidingAgain / GettingOut / Found / Final.

Communication via the inter-controller events Turn, Stop, Resume on the Actuator's `send(...)` channel; the Vehicle-surface Flag event leaves the system through `actuator.flag()`.

## Design choices

- `Sensor` exposes the four required RoboChart functions (`analysis`, `intensity`, `location`, `goreq`) as pure methods. The gas-reading parameter is named `reading` to avoid the controller-state `gs` shadow.
- Direction mapping for `location` is round-robin Front / Right / Back / Left over the 1-based sensor index.
- Constants set to plausible runtime defaults (`thr=5.0`, `lv=1.0`, `evadeTime=2`, `stuckPeriod=5`, `stuckDist=1.0`, `outPeriod=3`).
- Stuck-detection guards expressed only with `<` and `>` (the M2M's `since(...)` rewrite only matches `<`); the `stuck` branch is `!progress`, so the two autonomous transitions partition the state space.
- Per CLAUDE.md, the clock field `T` is reset via `T = clock.nowMs()`, captured by the M2M as `# T`.
