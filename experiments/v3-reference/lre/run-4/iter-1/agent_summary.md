# LRE, run 4 — iteration 1

No Java was present before writing.

## What was written

Fifteen files in `lre`:
- `annotation`: `RoboChartType`
- `event`: sealed `InputEvent` and `OutputEvent`
- `sensor`: `Obstacle`, `ObstacleRegister`, `Sensor`
- `actuator`: `Actuator.apply`
- `operation`: `CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`
- `controller`: `LreMode`, `LreController`
- base package: `LreConstants`

Each `step` first runs the five operations. It then declares named predicates (including `inOpez`) and applies the mode-nested if-else. The trace covers all 51 requirements.

## Decisions the requirements did not settle

- **"None" index.** `nat` cannot be −1, so index 0 means "none" and obstacle IDs start at 1.
- **HCM velocity.** Advises 0 m/s, following FR3 rather than the description's 0.1 m/s.
- **Safe large distance.** 1000000.0 m.
- **CPA.** Horizontal closing velocity w = AUV − obstacle. tcpa = p·w/(|w|²+ε), with ε = 0.000001; the model rounds it up to 1. cda is the distance at tcpa with the vertical separation kept.
- **vvel.** The signed rate of climb, as written.
- **Priority within a mode.** Operator events come first. Then inOpez, whose guard stays "alone"; then CAM; then HCM in the order Beh9, Beh11, Beh10, so that Beh11 can still be taken.
- **Closest index in operations.** `CheckOPEZ` and `CalcCPA` call the sensor's closest-index functions directly, because the profile forbids one operation reading another.
- **No input.** `step(null)` is a cycle with no operator input.
- **Start values and ties.** The actuator starts at 0. On a tie, the lowest obstacle index wins.

## Unresolved

- CAM's evasive advice is unspecified, so CAM issues no output.
- Beh10 and Beh13 use different vertical thresholds. They are disjoint only while the two thresholds are equal.

All of these are listed in `post_codegen.md`.
