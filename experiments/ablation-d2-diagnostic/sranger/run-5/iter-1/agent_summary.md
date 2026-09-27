# SRanger run 6 — iteration 1 (cold codegen)

**What changed.** The tree started cold: Gradle scaffold only, no Java, so nothing was deleted. I wrote 8 files under `java.generated.project/src/main/java/sranger/` (annotation, constants, mode, event, sensor, actuator, timing/Clock, controller), plus `result_codegen.json` with 31 rows covering all 23 requirements and every file, and `post_codegen.{md,json}`. `gradlew build` passes.

**Design.** `SRangerController.step(InputEvent)` is a mode-nested if-else with two named predicates, `obstacleDetected` and `turnDurationElapsed`, declared before the chain. Priorities:
- Moving: endTask → Final, then obstacle && obstacleDetected → Turning (resets `clockResetTime`, Move(0, turnVel)), then tick self-loop.
- Turning: endTask → Final, then the timed exit → Moving (Move(moveVel, 0)), then tick self-loop.

**Decisions the spec did not settle.**
1. SR-Beh2 names the obstacle event as the trigger, while SR-GP1 names `obstacleDetected` as the guard of the same transition. I used both.
2. Final is terminal (SR-FR3), but the codegen rules require every mode to have an outgoing transition. I added a no-action tick self-loop in Final, so it never leaves Final.
3. Move(lv, av) (SR-DM4) is implemented as `Actuator.move(lv, av)`, a two-argument call, rather than a two-field event record.
4. The time base is seconds, so turnDuration stays 2.0. The Clock accessor is named `nowMs()` to follow the documented clock convention, and its Javadoc states the unit. The controller field is `timer` because `clock` is a reserved word.
5. The "large default" distance with no reading (SR-DM5) is `Double.MAX_VALUE`. This is an invented default.
6. The initial Move(moveVel, 0) is issued in the constructor.

**Unresolved.** Nothing is blocked. Items 2–4 are the choices most likely to interact with the extractor. Whether they do, and how, will only show in the pipeline feedback.
