# SRanger run 1 — iteration 1 (cold codegen)

**Deleted:** nothing. `java.generated.project/src/main/java/` held no Java at all
(Gradle scaffold only), so there was no stale tree to clear.

**Wrote:** 8 Java files under `java.generated.project/src/main/java/sranger/`
(`annotation/RoboChartType`, `mode/SRangerMode`, `event/InputEvent`,
`constants/SRangerConstants`, `sensor/Sensor`, `actuator/Actuator`,
`time/Clock`, `controller/SRangerController`), plus `result_codegen.json`
(33 rows; all 23 requirement ids covered, all 8 files named) and
`post_codegen.{md,json}`. Compiles clean under `javac` 17.

Shape: single-method mode-nested `step(InputEvent)`; two named predicates
(`obstacleDetected`, `turnDurationElapsed`) declared before the chain; three
modes, each with an ungarded tick branch so every state has a
bare-precondition outgoing operation; `@RoboChartType("real")` on every
`double` field and parameter.

**Decisions the requirements did not settle.**

1. *Output encoding.* SR-DM4 calls `Move(lv, av)` an output **event**, but a
   RoboChart event carries one payload. I used a two-argument actuator call
   `actuator.move(lv, av)`, which the extractor models as a RoboChart `Call`
   in `LOperations` and so preserves both values; an `OutputEvent.Move` record
   would have had `av` silently dropped. No `OutputEvent` type exists.
2. *Turning priority.* SR-Beh5/6/7 give no relative order. Resolved as
   endTask > turnDurationElapsed > tick. The autonomous transition **must**
   precede the tick self-loop: tick arrives every control cycle, so putting it
   first would starve SR-Beh5 and the robot would turn forever.
3. *FINAL self-loop.* Added a tick-triggered `FINAL -> FINAL` branch not named
   by any SR-Beh requirement. Without it FINAL has no outgoing transition,
   which the theory generator's gate reports as an unprovable
   deadlock-freedom obligation. SR-DC1 still holds (no duplicate
   source/trigger pair).
4. *Units and defaults.* `Clock.nowSeconds()` returns seconds so
   `clockResetTime` and `turnDuration` share the spec's unit; the Sensor's
   missing-data default is 1000.0 m ("a large default value" is unquantified).

**Unresolved / noted, not fixed.** `obstacleThreshold = 0.5` will be ceiled to
`1` in the Constants interface (documented CSP-gen v3.0.0 limitation), so the
extracted guard is weaker than the Java one. That is an instrument limitation,
not something the Java can express around.
