# ITER_SUMMARY — sranger, pass 5, iteration 1

No Java was present before I started, so nothing was deleted.

## What I wrote

Nine files under `java.generated.project/src/main/java/sranger/`:

- `annotation/RoboChartType`
- `event/InputEvent`: Obstacle, Tick, EndTask
- `event/OutputEvent`: Move(lv, av)
- `sensor/Sensor`: distance(), update()
- `actuator/Actuator`: apply(), last-velocity getters
- `controller/SRangerMode`: MOVING (first), TURNING, FINAL
- `controller/SRangerConstants`: the four constants
- `controller/Clock`: now(), in seconds
- `controller/SRangerController`

`step()` declares two predicates, obstacleDetected and turnDurationElapsed (`now() - clockResetTime >= TURN_DURATION`, the form that becomes `since()`). Then comes one block per mode, with one branch per transition (SR-Beh2 to SR-Beh7). Final has none.

I also wrote `result_codegen.json` (65 entries, covering all 23 requirements and all 9 files) and `post_codegen.{md,json}`.

## Decisions the requirements left open

- Moving to Turning needs both the obstacle event (SR-Beh2) and the obstacleDetected guard (SR-GP1).
- endTask comes first in each mode. In Turning, the autonomous return to Moving precedes the tick self-loop, which conjoins !turnDurationElapsed.
- The constructor issues the power-up Move(moveVel, 0) (SR-FR1).
- Move has two real components, as SR-DM4 gives it.
- The clock field is named `timer` because `clock` is reserved.
- Invented defaults:
  - 1000.0 m when no reading is available (a negative or NaN reading counts as none);
  - clockResetTime starts at 0.0;
  - the actuator's stored velocities start at 0.0.
- Final has no tick self-loop, because no requirement gives one.

## Unresolved

- The model rounds OBSTACLE_THRESHOLD = 0.5 up to 1, so its obstacle guard accepts distances the Java rejects.
- I do not know how the extraction types an event with two components.
- I do not know whether the extraction keeps the constructor's initial Move.
