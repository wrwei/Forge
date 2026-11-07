# SRanger, run 5: iteration 1

## What I wrote

There are nine files under `sranger/`:
- the `RoboChartType` annotation;
- sealed `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`);
- `Sensor.distance()`;
- `Actuator.apply` and its last-velocity getters;
- in `controller/`: `SRangerMode` (MOVING, TURNING, FINAL), `SRangerConstants`, `Clock`, and `SRangerController`.

The controller has two predicates, `obstacleDetected` and `turnDurationElapsed`, and six `step()` branches, one per SR-Beh2 to SR-Beh7. FINAL has no transitions. The trace maps all 23 requirements and all 9 files.

## Decisions the requirements did not settle

- Moving → Turning fires on the `obstacle` event and needs `obstacleDetected` (SR-Beh2 with SR-GP1). The clock is reset before Move(0, turnVel) is issued.
- Priority in Turning: endTask comes first, then the timed exit, then tick, and the tick branch conjoins `!turnDurationElapsed`. tick arrives every cycle, so the timed exit has to come before it. endTask is also first in Moving.
- The constructor issues the initial Move(moveVel, 0) (SR-FR1, SR-Beh1).
- The tick self-loops perform no action (SR-Beh4, SR-Beh7).
- Invented defaults:
  - the no-reading distance is 1000.0 m;
  - a negative or NaN reading counts as "no reading";
  - `clockResetTime` starts at 0.0;
  - the actuator holds 0/0 before the first command.
- `Clock.now()` returns monotonic seconds since construction. The field holding the clock is named `timer`, because `clock` is reserved.

## Unresolved

- OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model's constants interface, so the model's guard reads distance <= 1.
- I don't know yet whether the extractor models the constructor's initial command.
