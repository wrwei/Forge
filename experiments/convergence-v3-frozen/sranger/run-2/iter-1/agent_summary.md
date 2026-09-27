# SRanger run 2 — iteration 1 (cold codegen)

**Changed.** Wrote the whole tree from nothing: 8 Java files under
`java.generated.project/src/main/java/sranger/` (`annotation/RoboChartType`,
`clock/Clock`, `mode/SRangerMode`, `constants/SRangerConstants`,
`event/InputEvent`, `sensor/IrSensor`, `actuator/Actuator`,
`controller/SRangerController`) plus `result_codegen.json` (52 entries, all
23 requirement ids) and `post_codegen.{md,json}`. **Nothing was deleted** —
`src/main/java` was empty. `javac --release 17` is clean.

**Decisions the requirements did not settle.**

1. *SR-DM4's two-real Move.* A RoboChart event carries one payload, and
   `actuator.apply(new OutputEvent.Move(lv, av))` would silently drop the
   angular velocity (the M2M keeps only the first constructor argument). I
   used `Actuator.move(double lv, double av)`, which the M2M extracts as an
   `LOperations` call carrying both. No sealed `OutputEvent` was created:
   unused by the controller it would still surface as a spurious
   `datatype Move` in the `.rct`. SR-DM4 maps to `Actuator.move`.

2. *Priority in Turning.* SR-Beh5 (autonomous, timed) and SR-Beh6 (endTask)
   both leave Turning, order unspecified. `endTask` goes first, per the
   project rule that operator overrides lead every mode block; this will
   raise preflight `rule8` as a warning. SR-Beh7's tick self-loop follows
   SR-Beh5 and carries `&& !turnDurationElapsed` — tick arrives every cycle,
   so the reverse order would starve the timed transition.

3. *Final kept live.* SR-FR3's terminal mode has a tick-triggered self-loop,
   not a bare `else`: neither an Isabelle deadlock nor an FDR4 divergence.

4. *Invented defaults.* `Clock.now()` in seconds, keeping `TURN_DURATION` at
   exactly 2.0; `IrSensor` returns 1000.0 m before its first reading
   (SR-DM5 says "a large default value" without a figure).

**Unresolved.** `OBSTACLE_THRESHOLD = 0.5` will be ceiled to 1 in the emitted
`.rct` constant (CSP-gen v3.0.0 cannot compile `FloatExp`). Instrument
limitation; no Java change avoids it without contradicting SR-DM2's unit.
