# SRanger run 5 — iteration 1 (cold codegen)

## What I wrote

`java.generated.project/src/main/java/` was empty — nothing deleted, no
stale tree from another study. I created 9 files under `sranger/`:
`annotation/RoboChartType`, `mode/SRangerMode` (MOVING/TURNING/FINAL),
`event/InputEvent` (sealed: Obstacle, Tick, EndTask), `event/OutputEvent`
(sealed: Move(lv, av)), `constants/SRangerConstants`, `sensor/IrSensor`,
`actuator/Actuator`, `time/Clock`, `controller/SRangerController`. Plus
`result_codegen.json` (49 entries, all 23 requirement ids, every file
named) and `post_codegen.{md,json}`. `./gradlew build` is clean.

The controller is single-method mode-nested if-else with two named
predicates. `clockResetTime` is a non-final double assigned from
`cycleClock.nowSeconds()` in `step()`, so the M2M promotes it to a
RoboChart clock and rewrites the timeout guard to `since(clockResetTime)
>= turnDuration`. FINAL gets a tick-triggered self-loop per
`java_codegen_rules.txt`.

## Decisions the requirements did not settle

1. **Turning branch order.** SR-Beh5 (autonomous timeout) and SR-Beh6
   (endTask) have no stated priority. I put the triggerless timeout branch
   FIRST and conjoined `&& !turnDurationElapsed` to the two event branches.
   endTask-first trips preflight rule8 (event branch before triggerless =
   both enabled in the extracted model); this order instead gives Turning a
   total guard cover, deadlock-free without a bare self-loop. Cost: an
   endTask coincident with the timeout is served one step later.
2. **Move(lv, av).** RoboChart events carry one payload and the M2M keeps
   only the first constructor argument, so `new OutputEvent.Move(lv, av)`
   would lose `av`. The controller calls `actuator.move(lv, av)` (2-arg →
   LOperations call, both values kept); `OutputEvent.Move` remains as the
   Actuator's stored command type.
3. **SR-Beh2 vs SR-GP1** — one fires on the obstacle event, the other
   guards on `obstacleDetected`. Implemented as trigger AND guard.
4. **Invented defaults**: `NO_READING_DISTANCE = 1000.0` m; clock in
   seconds, matching SR-Var1's unit.

## Unresolved

`obstacleThreshold = 0.5` reaches CSP as 1 (CSP-gen ceils fractions) — a
disclosed instrument limitation, not fixable in Java.
