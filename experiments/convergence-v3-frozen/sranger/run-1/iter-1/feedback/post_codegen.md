# post_codegen — SRanger, iteration 1

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented across 8 Java files
under `src/main/java/sranger/`. Four points need human review: the output
encoding of `Move(lv, av)`, the added FINAL tick self-loop, the branch
priority inside Turning, and the clock unit choice.

## Issues

### 1. `Move(lv, av)` modelled as a two-argument actuator call, not an OutputEvent record
- **kind:** design_choice
- **requirements:** SR-DM4, SR-DM6
- RoboChart events carry a single payload, so a two-real output cannot be an
  event. The extractor's documented route for a two-value output is a
  multi-argument method call, which becomes a RoboChart `Call` in an
  `LOperations` interface. The controller therefore calls
  `actuator.move(lv, av)` and no `OutputEvent` sealed interface exists.
- **fix_directive:** If SR-DM4 must surface as an `OutputEvent.Move` record,
  say so — but note the single-payload constraint would then drop `av`.

### 2. FINAL carries a tick self-loop that no SR-Beh requirement specifies
- **kind:** scope_question
- **requirements:** SR-FR3, SR-DC1
- A mode with no outgoing transition is a genuine deadlock in the extracted
  Z-machine, so `java_codegen_rules.txt` requires a tick-triggered self-loop
  on terminal modes. This adds an eighth transition beyond SR-Beh1..7.
  SR-DC1 is not violated (no duplicate source/trigger pair), but the
  transition is not in the spec.

### 3. Branch priority inside Turning
- **kind:** ambiguous_requirement
- **requirements:** SR-Beh5, SR-Beh6, SR-Beh7
- The spec gives no priority among the three Turning transitions. Resolved as
  endTask > turnDurationElapsed > tick. The autonomous transition must precede
  the tick self-loop, otherwise the per-cycle tick starves it and the robot
  turns forever.

### 4. Clock exposes seconds, not milliseconds
- **kind:** invented_default
- **requirements:** SR-Var1, SR-GP2
- `Clock.nowSeconds()` returns seconds so `clockResetTime` and `TURN_DURATION`
  share the spec's unit and no derived millisecond constant is needed.

### 5. Sensor missing-data default
- **kind:** invented_default
- **requirements:** SR-DM5
- SR-DM5 says "a large default value" without fixing one; `1000.0` m is used.

**next_step:** run the pipeline (compile → coverage → preflight → T2M → M2M →
M2T → verifiers).
