# post_codegen — SRanger iteration 1

**Status:** uncertain

Cold SRanger codegen: 9 files, 3 modes, 7 spec transitions plus a Final tick self-loop. 5 items flagged for review.

## 1. [design_choice] Final mode given a tick self-loop

- Raw: SR-FR3 describes Final as terminal and lists no outgoing transition; the codegen rules require every mode to have one.
- Directive: Confirm that Final --tick--> Final (no action) is acceptable. It is an additional transition not enumerated in SR-Beh1..7 (SR-DC1).
- Trace: `src/main/java/sranger/controller/SRangerController.java#step` (SR-FR3, SR-DC1)

## 2. [ambiguous_requirement] Moving->Turning: event AND guard

- Raw: SR-Beh2 fires on the obstacle event; SR-GP1 says obstacleDetected guards the same transition.
- Directive: Implemented as obstacle event && obstacleDetected. An obstacle event with distance above threshold is ignored.
- Trace: `src/main/java/sranger/controller/SRangerController.java#step` (SR-Beh2, SR-GP1)

## 3. [design_choice] Clock unit vs method name

- Raw: Spec times are in seconds; the pipeline's clock-read convention is nowMs().
- Directive: Clock.nowMs() reports time in the constants' unit (seconds) so TURN_DURATION=2.0 means 2 s.
- Trace: `src/main/java/sranger/timing/Clock.java#nowMs` (SR-Var1, SR-GP2)

## 4. [invented_default] Sensor no-reading default 1.0e6 m

- Raw: SR-DM5 requires a 'large default value' without a number.
- Directive: Default is 1.0e6 m; NaN/negative measurements are treated as no reading.
- Trace: `src/main/java/sranger/sensor/Sensor.java#distance` (SR-DM5)

## 5. [design_choice] Initial Move issued in constructor

- Raw: SR-FR1: on entering Moving (including power-up) issue Move(moveVel,0).
- Directive: The constructor issues Move(MOVE_VEL, 0.0); step() transitions into Moving issue it inline.
- Trace: `src/main/java/sranger/controller/SRangerController.java#SRangerController` (SR-FR1, SR-Beh1)

**Next step:** Run the pipeline via scripts/run_claude_iteration.sh.
