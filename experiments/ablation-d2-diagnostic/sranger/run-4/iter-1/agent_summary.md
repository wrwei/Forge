# SRanger run 5 — iteration 1 (cold codegen)

**Changed.** Created `java.generated.project/src/main/java/sranger/` from nothing (no pre-existing Java; nothing deleted). 9 files: `annotation/RoboChartType`, `constants/SRangerConstants`, `mode/SRangerMode` (MOVING initial, TURNING, FINAL), `event/InputEvent` (Obstacle, Tick, EndTask), `event/OutputEvent` (Move(lv, av)), `sensor/Sensor` (`distance()`), `actuator/Actuator` (`apply`), `timing/Clock` (`nowMs()`), and `controller/SRangerController` with a single mode-nested `step()`. Also wrote `result_codegen.json` (all 23 requirements, every file cited) and `post_codegen.{md,json}`. No `operation` package, because no requirement has type `operation`. The build compiles and a scratch behaviour check passes.

**Decisions the spec did not settle.**
- Final is described as terminal, but the codegen rules require every mode to have an outgoing transition. I added Final --tick--> Final with no action. This is one transition beyond SR-Beh1..7 (see SR-DC1).
- SR-Beh2 triggers on the obstacle event, while SR-GP1 says obstacleDetected guards that transition. I implemented the conjunction.
- Times are in seconds, but the documented clock-read convention is `nowMs()`. The Clock reports time in the constants' unit (seconds), so `timer.nowMs() - clockResetTime >= TURN_DURATION` means 2 s. The clock field is named `timer` to avoid the reserved word `clock`.
- The "large default" for a missing IR reading is set to 1.0e6 m. NaN or negative readings are treated as missing.
- The power-up Move(moveVel, 0) required by SR-FR1 is issued in the constructor.
- Priority is endTask, then the obstacle event or turn timeout, then tick.

**Unresolved.** It is unknown whether the extractor handles a two-field output event (Move(lv, av)) as a single event. The pipeline feedback will show this.
