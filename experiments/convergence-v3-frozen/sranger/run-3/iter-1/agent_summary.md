# sranger / run 3 / iteration 1 — cold codegen

## What I wrote

`src/main/java/` was empty (Gradle scaffold only), so nothing was deleted.
Nine new files under `sranger/`: `annotation/RoboChartType`,
`constants/SRangerConstants`, `mode/SRangerMode`, `event/InputEvent` (sealed:
Obstacle/Tick/EndTask), `event/OutputEvent` (sealed: Move), `sensor/Sensor`,
`actuator/Actuator`, `time/Clock`, `controller/SRangerController`. Plus
`result_codegen.json` (23/23 requirements traced, all 9 files named) and
`post_codegen.{md,json}`. `./gradlew build` clean. The stale `chemdetector`
group in `build.gradle` was left alone.

One `step(InputEvent)` with two named predicates (`obstacleDetected`,
`turnDurationElapsed`) and three `currentMode == X` blocks. `clockResetTime` is
a plain controller `double` assigned from `cycleClock.nowSeconds()` in the
Moving→Turning branch, so the M2M promotes it to a RoboChart `clock` and
rewrites the guard to `since(clockResetTime) >= turnDuration`. No operation
class: `java2robochart.etl` documents a `TurnTimer` operation holding that
timestamp as un-liftable (written from a transition, not `compute()`, so it
stubs to `0`), and the spec declares no `operation` type.

## Decisions the requirements did not settle

1. **FINAL left with no outgoing transition.** `java_codegen_rules.txt` wants a
   Tick self-loop on sink modes; SR-Beh1–7 + SR-DC1 specify exactly seven
   transitions, none leaving Final. Followed the spec: the frozen generator's
   The fix emits `until (st = FINAL)`, exempting such states from `deadlock_free`,
   and `vacuity.py` grades `dead_state_no_offer` advisory for a specified
   terminal mode. If 6c/FDR4 disagree, iteration 2 adds the loop.
2. **`endTask` ordered above the autonomous turn-complete transition** (operator
   shutdown). Knowingly trips preflight `rule8` (warning) — same shape as the
   documented LRE CAM block; the alternative drops `endTask` on the
   turn-completion cycle.
3. **Sensor no-reading default 1000.0 m** — "large", magnitude unspecified.

## Unresolved

`Move(lv, av)` is faithful to SR-DM4, but the M2M keeps only the first
constructor argument, so angular velocity never reaches the formal model. An
instrument limitation; SR-DM4 forbids splitting into two events.
