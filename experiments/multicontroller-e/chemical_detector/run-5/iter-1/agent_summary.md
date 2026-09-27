# Iteration 1 — chemical_detector run 5 (condition E), cold codegen

**What changed.** New tree `java.generated.project/src/main/java/chemical_detector/`, 18 files. `src/main/java` held no Java beforehand, so nothing was deleted. The main pieces:
- Two mode-nested controllers: `GasAnalysisController` (Reading, Analysis, NoGas, GasDetected, Concluded) and `MovementController` (Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut).
- Sealed `InputEvent`/`OutputEvent` with lowercase records named after the spec events, so turn/stop/resume match across the two controllers.
- `Sensor` holding the functions analysis/intensity/location/goreq and odometer.
- `Vehicle` with move/randomWalk/shortRandomWalk/pause, the `ChangeDirection` operation, and `Clock` with the evasion clock `T`.
- A wiring class plus `result_codegen.json` (all 81 requirements) and `post_codegen.{md,json}`.

**Decisions the requirements did not settle.**
1. **No Final states.** The spec sends GasDetected and Found to a final state. I kept every mode progressable instead:
   - GasAnalysis ends in `Concluded`, where a gas event only replaces `gs` (CD-GA-Var1) and no analysis runs.
   - Movement's `Found` keeps a stop self-loop with no actions, mirroring the resume self-loop in Waiting (CD-MV-Beh3), so flag and the halt fire once.
2. **Complementary guards** (`P` / `!P`) on every autonomous choice: Analysis, GasDetected, and AvoidingAgain's progress vs stuck.
3. **Invented values:**
   - Constants: THR=1, LV=1, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1, OUT_PERIOD=1, plus HALT_VELOCITY=0.
   - "Indicates target" means c == target and i > 0.
   - Sensor position 1..4 maps to Left, Right, Back, Front, repeating.
4. **Types:** Chem is a nat and Intensity is a real (no wrapper records).
5. **Operation placement:** move/randomWalk/shortRandomWalk are Vehicle methods. `changeDirection(l)` is an operation class with no `compute()`, because it is an actuation that takes a parameter.
6. **Odometer** is sampled through `sensor.odometer()`, not received as a trigger event.

**Unresolved.** Whether the extractor turns `changeDirection(l)`, `randomWalk()` and `shortRandomWalk()` into operation calls, and what the vacuity audit accepts as an invariant. Feedback will show both.
