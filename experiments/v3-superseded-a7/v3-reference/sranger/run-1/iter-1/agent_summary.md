# ITER_SUMMARY — sranger, run 1

## Iteration 1

**What I wrote.** Package `sranger`, nine files:
- `annotation/RoboChartType`
- `event/InputEvent` (Obstacle, Tick, EndTask) and `event/OutputEvent` (Move(lv, av))
- `sensor/Sensor` (`distance()`, `update()`) and `sensor/Clock` (`now()`, `advance()`)
- `actuator/Actuator` (`apply()`, last lv/av)
- `controller/SRangerMode` (MOVING, TURNING, FINAL), `controller/SRangerConstants` and `controller/SRangerController`

`step()` declares two predicates, `obstacleDetected` and `turnDurationElapsed`. It has one block per mode and seven transitions (SR-Beh1..7), one per (mode, trigger) pair (SR-DC1). The trace has 23/23 requirements and 9/9 files.

**Decisions the requirements did not settle:**
- **Moving→Turning** is triggered by `obstacle` and guarded by `obstacleDetected`, which joins SR-Beh2 and SR-GP1.
- **Turning priority:** endTask first, then the autonomous timeout, then tick guarded by `!turnDurationElapsed`. tick arrives every cycle, so putting it first would mean Turning is never left.
- **Initial forward command:** the constructor issues `Move(moveVel, 0)` at power-up (SR-FR1). The model may omit it, because the profile only extracts entry actions from `step()` branches.
- **Time source:** `Clock` is advanced by the framework, in seconds. The field is named `timer` because `clock` is reserved.
- **Invented defaults:** the no-reading distance is 1000.0 m, and a negative or NaN reading also falls back to it. The actuator starts at (0, 0).
- **Final** is terminal. Other events are ignored in Turning and Final.

**Unresolved:**
- The model rounds `obstacleThreshold = 0.5` up to 1. This does not change the deadlock or divergence results, but the guard's exact value does not carry over to the model.
- Move has two real components. I cannot tell from the rules how a two-component event is typed in the model.

Details are in `post_codegen.md`.
