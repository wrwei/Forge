# sranger run 2 — iteration 1 (cold codegen)

**Deleted:** nothing. The tree started cold (no Java present).

**Written (9 Java files + trace):** package `sranger` with `annotation/RoboChartType`, `constants/SRangerConstants`, `mode/SRangerMode` (MOVING/TURNING/FINAL), `event/InputEvent` (Obstacle, Tick, EndTask), `event/OutputEvent` (Move(lv, av)), `sensor/Sensor` (distance(), large default 1000 m before the first reading), `actuator/Actuator` (stores the last Move), `time/Clock` (nowMs()), and `controller/SRangerController`. It has a single `step()`, two named predicates (`obstacleDetected`, `turnDurationElapsed`), and one mode-nested if-else chain. `result_codegen.json` maps all 23 requirements (32 rows) and names every file.

**Decisions the spec did not settle:**
- *Clock unit.* I followed CLAUDE.md's `clock.nowMs()` convention, so time is in milliseconds. That required an invented derived constant, `TURN_DURATION_MS = 2000.0`, alongside the spec's `TURN_DURATION = 2.0` s.
- *Final needs an outgoing transition.* The spec says Final is terminal. The codegen rules say every mode needs an outgoing transition. I added a Final→Final self-loop on `tick` that has no action and no mode change.
- *Obstacle guard.* SR-Beh2 is triggered by an event, and SR-GP1 is a guard on that same transition. The transition fires on `obstacle && obstacleDetected`.
- *Priority in Turning.* The order is endTask, then the timed return to Moving, then tick. If tick came first, the timed transition could never fire, because tick arrives every cycle.
- *Tick self-loops.* These assign the same mode again and do nothing else (SR-Beh4, SR-Beh7).
- *Initial entry action.* Moving's entry action, Move(moveVel, 0), is issued in the constructor.

**Unresolved / for review:**
- The guard-predicate trace rows (SR-GP1/2) name local variables in `step()`, as `codegen_trace_rules.txt` instructs. No valid `element_type` fits a local, so I used `field`.
- `build.gradle` still has `group = 'chemdetector'`. I left it alone, as instructed.
