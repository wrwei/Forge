# sranger run 3 — iteration 1 (cold codegen)

**What changed.** The tree started cold: `java.generated.project/src` did not exist, so nothing was deleted. I wrote 9 Java files under `sranger/`:
- `annotation/RoboChartType`
- `mode/SRangerMode` (MOVING, TURNING, FINAL)
- `constants/SRangerConstants`
- `event/InputEvent` (Obstacle, Tick, EndTask)
- `event/OutputEvent` (Move(lv, av))
- `sensor/Sensor` (`distance()`)
- `actuator/Actuator` (`apply`, last-command accessors)
- `timing/Clock` (`nowMs()`)
- `controller/SRangerController` (a mode-nested `step()` with the named predicates `obstacleDetected` and `turnDurationElapsed`)

I also wrote `result_codegen.json` with 38 entries covering all 23 requirements and every file, plus `post_codegen.{md,json}`. The project compiles.

**Decisions the requirements did not settle.**
1. *Final's outgoing transition.* SR-FR3 makes Final terminal, but the codegen rules forbid a mode with no enabled transition. I added a tick-triggered Final→Final self-loop with no action. Final is still never left. This is one transition beyond SR-Beh1..7.
2. *Clock units.* The extractor's convention reads time through `Clock.nowMs()`, but the spec works in seconds. `nowMs()` returns seconds so that turnDuration stays 2.0 instead of 2000. The name does not match the unit.
3. *Moving→Turning trigger.* SR-Beh2 names the obstacle event and SR-GP1 names obstacleDetected as the guard of the same transition. I implemented both: trigger obstacle, guard `distance() <= obstacleThreshold`.
4. *Priorities.* endTask comes first in every mode. In Turning the autonomous duration check comes before the tick self-loop.
5. *Initial Move.* The constructor issues Move(moveVel, 0), because SR-FR1's entry action applies at power-up.
6. *No-reading default.* The sensor returns 1000.0 m. SR-DM5 only says "large".

**Unresolved.** It is not yet known how the extractor handles a two-field output event (`Move(lv, av)`) or the Final self-loop. The pipeline feedback will settle both.
