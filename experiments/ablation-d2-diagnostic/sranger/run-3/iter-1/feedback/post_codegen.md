# post_codegen — sranger iteration 1

**Status:** uncertain

Cold iteration-1 codegen for sranger: 9 Java files, all 23 requirements traced; 6 judgement calls surfaced for review.

## 1. [design_choice] Final has a tick self-loop not named by any requirement

- **Raw:** SR-FR3 calls Final terminal; SR-DC1 says SR-Beh1..7 are the only transitions. The codegen rules require every mode to have an outgoing transition.
- **Fix directive:** Confirm Final should accept tick as a no-action self-transition (an 8th transition with no requirement), or specify Final's intended behaviour.
- **Trace:** `src/main/java/sranger/controller/SRangerController.java:76-80` (step) — SR-FR3, SR-DC1

## 2. [ambiguous_requirement] Moving -> Turning: event trigger vs guard

- **Raw:** SR-Beh2 fires on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) guards the same transition.
- **Fix directive:** Implemented as obstacle event AND obstacleDetected. Confirm the conjunction is intended.
- **Trace:** `src/main/java/sranger/controller/SRangerController.java:52-62` (step) — SR-Beh2, SR-GP1

## 3. [design_choice] Clock time base is milliseconds

- **Raw:** SR-Var1 states clockResetTime in seconds; the Clock dependency exposes nowMs().
- **Fix directive:** clockResetTime is stored in ms and compared with TURN_DURATION_MS = 2000.0 (= turnDuration 2.0 s). Confirm or require a seconds time base.
- **Trace:** `src/main/java/sranger/controller/SRangerController.java:53-61` (step) — SR-Var1, SR-GP2, SR-DM2

## 4. [invented_default] No-reading distance default = 1000 m

- **Raw:** SR-DM5 asks for 'a large default value' without a number.
- **Fix directive:** NO_READING_DISTANCE = 1000.0 in SRangerConstants; negative readings clamp to 0.
- **Trace:** `src/main/java/sranger/sensor/Sensor.java:9-32` (Sensor) — SR-DM5, SR-SF1

## 5. [design_choice] Transition priority

- **Raw:** Requirements give no priority between endTask, obstacle, the timed transition and tick.
- **Fix directive:** endTask first in every mode; in Turning the timed transition outranks the tick self-loop.
- **Trace:** `src/main/java/sranger/controller/SRangerController.java:55-80` (step) — SR-Beh2, SR-Beh3, SR-Beh5, SR-Beh6, SR-Beh7

## 6. [design_choice] Initial Moving entry command issued in the constructor

- **Raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving, including at power-up (SR-Beh1).
- **Fix directive:** The constructor issues Move(MOVE_VEL, 0.0). Confirm.
- **Trace:** `src/main/java/sranger/controller/SRangerController.java:29-35` (SRangerController) — SR-FR1, SR-Beh1

**Next step:** Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers).
