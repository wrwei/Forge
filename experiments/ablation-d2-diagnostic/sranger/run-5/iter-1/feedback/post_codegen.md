# post_codegen

**Status:** uncertain

Cold SRanger codegen: 8 files, 3 modes, 7 required transitions plus a Final tick self-loop. Six review items.

## 1. [ambiguous_requirement] Moving->Turning: obstacle event AND obstacleDetected guard

- Raw: SR-Beh2 triggers on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) guards the same transition.
- Fix directive: Implemented as trigger Obstacle with guard obstacleDetected. If the guard is meant to be informational only, drop '&& obstacleDetected'.
- Trace: `src/main/java/sranger/controller/SRangerController.java` / `step` — SR-Beh2, SR-GP1

## 2. [design_choice] Final mode has a tick self-loop

- Raw: SR-FR3 makes Final terminal with no listed transitions; the codegen rules require every mode to have an outgoing transition.
- Fix directive: Added a no-action Tick self-loop in FINAL (never leaves Final). Remove if Final must refuse all events.
- Trace: `src/main/java/sranger/controller/SRangerController.java` / `step` — SR-FR3, SR-DC1

## 3. [design_choice] Move output realised as Actuator.move(lv, av)

- Raw: SR-DM4 describes Move as an output event with two reals.
- Fix directive: Implemented as a two-argument actuator call (an operation in RoboChart terms, as in the original SRanger model) rather than a two-field event record.
- Trace: `src/main/java/sranger/actuator/Actuator.java` / `move` — SR-DM4, SR-DM6

## 4. [design_choice] Clock time base is seconds; accessor named nowMs()

- Raw: SR-Var1/SR-DM2 give clockResetTime and turnDuration in seconds; the pipeline clock convention uses clock.nowMs().
- Fix directive: Clock.nowMs() returns seconds (documented) so turnDuration stays 2.0; controller field is 'timer' to avoid the reserved word 'clock'.
- Trace: `src/main/java/sranger/timing/Clock.java` / `nowMs` — SR-Var1, SR-GP2

## 5. [design_choice] Initial Move(moveVel, 0) issued in constructor

- Raw: SR-FR1: on entering Moving issue Move(moveVel,0); Moving is initial (SR-Beh1).
- Fix directive: The constructor issues the initial forward Move. Transition priority: endTask first in every mode; in Turning the timed exit precedes the tick self-loop.
- Trace: `src/main/java/sranger/controller/SRangerController.java` / `SRangerController` — SR-FR1, SR-Beh1

## 6. [invented_default] No-reading distance default = Double.MAX_VALUE

- Raw: SR-DM5 requires 'a large default value' without a number.
- Fix directive: Sensor returns Double.MAX_VALUE until the first update; negative readings are clamped to 0.
- Trace: `src/main/java/sranger/sensor/Sensor.java` / `distance` — SR-DM5, SR-SF1

## Next step

Run the pipeline; revisit these choices only if a verifier implicates them.
