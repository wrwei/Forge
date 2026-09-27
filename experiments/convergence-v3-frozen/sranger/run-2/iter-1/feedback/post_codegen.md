# post_codegen — SRanger, iteration 1

status: uncertain

## Summary

23/23 requirements implemented across 8 Java files under
`java.generated.project/src/main/java/sranger/`. `javac --release 17`
compiles clean. Four points need human review; none blocked the codegen.

## Issues

### 1. SR-DM4 realised as a two-argument actuator call, not an event record
kind: design_choice

`Move(lv, av)` carries two reals. A RoboChart event has a single payload
type, so an `OutputEvent.Move` record reached by `actuator.apply(...)`
would have silently dropped `av` (the M2M takes only the first
constructor argument). Realised instead as
`Actuator.move(double lv, double av)`, which the M2M extracts as an
`LOperations` operation call carrying both values.

fix_directive: If SR-DM4 must be a sealed-interface event record, say so
and accept that the extracted model carries only the linear velocity.

java_trace: src/main/java/sranger/actuator/Actuator.java (move) —
requirement_ids: [SR-DM4]

### 2. Shutdown priority vs. the timed transition in Turning
kind: ambiguous_requirement

SR-Beh5 (timed, autonomous) and SR-Beh6 (endTask) both leave Turning; the
requirements state no priority. `endTask` is placed first, per the
project rule that operator overrides lead every mode block. Consequence:
the preflight linter's `rule8` (event-triggered branch before a
triggerless one) will warn on the Turning block.

java_trace: src/main/java/sranger/controller/SRangerController.java (step)
— requirement_ids: [SR-Beh5, SR-Beh6]

### 3. Turning tick self-loop ordered after the timed transition
kind: design_choice

SR-Beh7's tick self-loop is placed *after* SR-Beh5 and carries
`&& !turnDurationElapsed`. Tick is delivered every control cycle, so a
tick branch above the timed guard would starve SR-Beh5 entirely.

java_trace: src/main/java/sranger/controller/SRangerController.java (step)
— requirement_ids: [SR-Beh5, SR-Beh7]

### 4. Clock unit and the "large default" sensor reading
kind: invented_default

`Clock.now()` returns seconds so `TURN_DURATION` stays 2.0 exactly as
specified. SR-DM5 asks for "a large default value" with no number;
1000.0 m is used.

java_trace: src/main/java/sranger/clock/Clock.java (now),
src/main/java/sranger/sensor/IrSensor.java (latestDistance) —
requirement_ids: [SR-DM5, SR-Var1]
