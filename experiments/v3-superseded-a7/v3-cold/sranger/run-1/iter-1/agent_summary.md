# Iteration 1 summary: SRanger, cold pass

No Java was present beforehand, so nothing was deleted.

**What was written.** Nine Java files under `sranger/`:

- the `RoboChartType` annotation;
- sealed `InputEvent` (Obstacle, Tick, EndTask) and `OutputEvent` (Move(lv, av));
- `Sensor` (distance, update) and `Actuator` (apply, last velocities);
- in `controller`: `SRangerMode` (MOVING first), `SRangerConstants` (the four real constants), `Clock` and `SRangerController`.

`step` declares two predicates, `obstacleDetected` and `turnDurationElapsed`, and has one block per mode:

- Moving handles endTask, obstacle and tick.
- Turning handles endTask, the timed return and tick.
- Final is terminal.

Also written: `result_codegen.json` (55 entries; every requirement and file) and `post_codegen.{md,json}` (8 items). The code compiles with `javac --release 17`, and a jshell walk-through behaved as specified.

**Decisions the requirements did not settle:**

- Moving to Turning needs both the Obstacle event (SR-Beh2) and the guard `distance <= 0.5` (SR-GP1). An obstacle event while the distance is above 0.5 m is ignored.
- Turning priority: endTask, then the timed return, then tick conjoined with `!turnDurationElapsed`.
- The initial Move(moveVel, 0) is issued in the constructor (SR-FR1).
- Move keeps two components, as SR-DM4 states.
- The controller's Clock field is named `timer`, because `clock` is reserved. Clock is a seconds counter that the framework advances.
- `clockResetTime` is reset before the turn command, in the order SR-Beh2 gives.
- With no reading, distance is 100.0 m. A negative or NaN reading counts as no reading. The actuator starts at (0, 0).

**Unresolved:**

- OBSTACLE_THRESHOLD = 0.5 is fractional, and the model rounds it up to 1, so the model's obstacle guard is wider than the Java one. I kept the specified value.
- I cannot control whether the extracted model's initial transition carries the constructor's Move command.
