# SRanger run 6 — iteration 2

**Feedback from iteration 1.** 11 of 12 phases passed. Only dafny_verify failed, with `SRangerController.dfy(10): unresolved identifier: MAX_VALUE`.

**Diagnosis.** The only `MAX_VALUE` in the tree is the no-reading default in `Sensor`, `NO_READING_DISTANCE = Double.MAX_VALUE` (SR-DM5). The Dafny generator emitted the Java library constant as a bare identifier, and Dafny cannot resolve it. This is the invented default I flagged in iteration 1, not a logic defect.

**Change.** One line in `sensor/Sensor.java`: `NO_READING_DISTANCE = 1000000.0`. This is a plain finite literal, far above the 0.5 m obstacle threshold. That keeps the SR-DM5 intent that missing data never triggers obstacle detection. I avoided exponent notation such as `1.0e6` in case Dafny's real literals reject it. I updated `post_codegen.{md,json}` to match. The build passes, and no other `Double.*` or `Math.*` references remain in the tree.

**Decisions.** The value 1000000.0 m is still invented, because the spec says only "large". The controller, transitions and trace are unchanged. The iteration-1 design choices still stand: obstacle trigger plus guard, a tick self-loop in Final, `Actuator.move(lv, av)`, and a seconds time base behind `Clock.nowMs()`. FDR4 and the Isabelle proofs passed with them.

**Unresolved.** None.
