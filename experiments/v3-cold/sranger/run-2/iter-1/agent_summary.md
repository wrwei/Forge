# SRanger — cold pass 2, iteration 1

## What was written

Nine Java files under `java.generated.project/src/main/java/sranger/`:

- `annotation/RoboChartType`
- `event/InputEvent`: `Obstacle`, `Tick`, `EndTask`
- `event/OutputEvent`: `Move(lv, av)`
- `sensor/Sensor`: `distance()`
- `sensor/Clock`
- `actuator/Actuator`: stores the last command
- `controller/SRangerMode`, `controller/SRangerConstants`
- `controller/SRangerController`: one `step()` with predicates `obstacleDetected` and `turnDurationElapsed`, and seven transitions, one per SR-Beh requirement (SR-DC1)

Also written: `result_codegen.json` (all 23 requirements, all 9 files) and `post_codegen.{md,json}`. No Java was present beforehand. The code compiles with `javac --release 17 -Xlint:all` with no warnings. A scratch driver outside the project exercised every transition.

## Decisions the requirements left open

1. **Moving → Turning.** The branch tests the obstacle event (SR-Beh2) and `obstacleDetected` (SR-GP1).
2. **Priority.** `endTask` comes first in Moving and Turning. In Turning, the timed transition precedes the tick self-loop because tick arrives every cycle. The tick branch also tests `!turnDurationElapsed`.
3. **Power-up command.** The constructor issues the initial `Move(moveVel, 0)`, since no transition leads into the initial mode.
4. **Turning entry order.** The clock is reset before `Move(0, turnVel)`. SR-Beh2 and SR-FR2 disagree on this order.
5. **Invented defaults.**
   - No-reading distance: 1000.0 m. Negative or NaN readings count as no reading.
   - Actuator memory before any command: (0, 0).
   - `clockResetTime`: starts at 0.0.
6. **Naming.** The clock field is `timer` (`clock` is reserved). The actuator method is `apply` (`move` would clash with `Move`).

## Unresolved

- The model rounds `obstacleThreshold = 0.5` up to 1, so its version of SR-GP1 is weaker than the Java's. I kept the requirement's value.
- In RoboChart, the tick self-loops re-enter their state. If the model puts Move in entry actions, each re-entry would re-issue it.
