# LRE run 1 — iteration 3

## What changed and why
Iteration 2 fixed the Dafny/vacuity failures (the `1e-9` constant is gone) but
introduced two new ones, **m2t** and **isabelle_verify**, both from a single
name collision created by iteration 2's fix.

The extractor lifts operation `compute()` bodies into the Z-machine's update
block, so `CalcCPA`'s fields become state variables and the sensor calls become
RoboChart functions. Iteration 2 named the new Sensor helper
`closingSpeedSq(index)` — the same name as the `CalcCPA` field it feeds. The
generated `.rct` therefore contains both

    function closingSpeedSq ( index : int ) : real { }
    var closingSpeedSq : real
    action ... closingSpeedSq = closingSpeedSq ( cdyn ) ...

The state variable shadows the function, which is the documented "function-typed
variable application" crash in the RoboChart CSP generator (m2t produced no CSP
files), and in Isabelle the same line becomes
`LreController.closingSpeedSq<s> :: ℝ` applied to `cdyn<s> :: ℤ` —
"operator not of function type" at `LreController_Beh.thy:106`.

Fix: renamed the Sensor method to `safeClosingSpeedSq(index)`. The `CalcCPA`
field keeps its name. One line in `Sensor.java`, one in `CalcCPA.java`, and the
matching `result_codegen.json` entry. `./gradlew build` clean.

**Lesson worth keeping:** a sensor method referenced from an operation's
`compute()` must not share a name with any operation field, because the two land
in the same RoboChart namespace.

## Decisions not settled by the requirements
Unchanged and recorded in `post_codegen.{md,json}`: HCM entry velocity 0 vs
0.1 m/s (LRE-FR3 vs the system description); the `nsRelDist`/`ewRelDist`
no-obstacle default (LRE-DM5 says zero, LRE-OP5 says large-distance — followed
OP5, since zero forces a spurious permanent CAM); `SAFE_LARGE_DISTANCE = 1000.0`;
transition priority within each mode; `ObstacleRegister` as a dense list.

## Unresolved
The vacuity advisory `dead_state_no_offer (OCM, {})` is marked ADVISORY by the
audit itself — it follows from the CSP domain narrowing, not the Java.
