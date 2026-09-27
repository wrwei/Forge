# sranger run 4 — iteration 1 (cold codegen)

**Changed.** Wrote a fresh tree under `java.generated.project/src/main/java/sranger/`. There was no pre-existing Java, so nothing was deleted. The 9 files are: `annotation/RoboChartType`, `constants/SRangerConstants`, `mode/SRangerMode` (MOVING, TURNING, FINAL), `event/InputEvent` (Obstacle, Tick, EndTask), `event/OutputEvent` (Move(lv, av)), `sensor/Sensor` (distance()), `actuator/Actuator` (apply), `time/Clock` (nowMs()) and `controller/SRangerController` (single mode-nested `step()`). No requirement has the type `operation`, so there is no operation package. I also wrote `result_codegen.json`, which traces all 23 requirements and every file, and `post_codegen.{md,json}`. `gradlew build` is clean.

**Decisions the requirements did not settle:**
- **Final gets a no-action self-loop on tick.** The codegen rules require every mode to have an outgoing transition. SR-FR3 ("terminal") and SR-DC1 (only SR-Beh1..7) don't provide one, so this is an extra transition with no requirement.
- **Moving→Turning needs both the obstacle event and the obstacleDetected guard.** SR-Beh2 gives the trigger and SR-GP1 gives the guard, so I combined them.
- **The clock is in milliseconds.** The time source is `Clock.nowMs()` (the convention CLAUDE.md documents). So clockResetTime is stored in ms and compared with TURN_DURATION_MS = 2000.0. SR-Var1 says seconds. If this makes the model's clock bound too large, the fallback is a seconds time base.
- **Priority order.** endTask comes first in every mode. In Turning, the timed transition outranks tick.
- **Invented defaults and startup.** The sensor returns 1000 m when it has no reading. The constructor issues the initial Move(moveVel, 0).

**Unresolved.** Final's behaviour needs human review: the spec calls Final terminal, but the rules forbid a mode with nothing enabled.
