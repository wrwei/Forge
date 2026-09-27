# SRanger run 5 — iteration 2

**Input.** In iteration 1, every phase passed except preflight. It reported 1 error and 2 warnings.

**Changed (2 files).**
- `sensor/Sensor.java`: added `@RoboChartType("real")` to the `NO_READING_DISTANCE` double constant. This fixes the error, rule4_double_missing_real_annotation.
- `controller/SRangerController.java`: the Turning tick self-loop is now guarded by `!turnDurationElapsed`. This fixes rule6_missing_priority_negation. The negation is redundant in Java, because the else-if already implies it, but it makes the extracted preconditions for the timeout and tick transitions mutually exclusive, so the model matches the Java. There is still exactly one tick transition out of Turning, so SR-DC1 holds.

**Judgement: rule8 warning deliberately left.** In Turning, the endTask branch comes before the triggerless timeout branch. I kept this order. Putting the timeout first would make the Java discard an endTask that arrives in the same step the timeout expires, so an operator shutdown would be lost. That is a safety regression. With the current order the Java ends in Final with Move(0,0). This is the same result as the spec's timeout followed by endTask, minus a transient forward command. In the extracted model, endTask and the timeout are enabled together in Turning. The requirements themselves specify this (SR-Beh5 is autonomous and SR-Beh6 is endTask-triggered), and the Java's choice is one of the model's allowed behaviours. The alternative was to trigger the timeout on tick. I rejected it because it contradicts SR-Beh5 ("no event required") and would create two tick transitions out of Turning, which breaks SR-DC1.

**Carried over from iteration 1 (still open for human review).** Final has a tick self-loop, which is not in the spec's transitions. Moving→Turning requires both the obstacle event and obstacleDetected. `Clock.nowMs()` reports seconds. The no-reading default is 1.0e6 m.
