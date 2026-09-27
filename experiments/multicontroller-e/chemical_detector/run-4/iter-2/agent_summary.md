# Iteration 2 — chemical_detector run 4

**Result of iteration 1:** 11 of 12 phases passed: compile, coverage, T2M, M2M, M2T, both generators, FDR4, Dafny, Isabelle and vacuity. Only preflight failed.

**Changed (2 files):**
- `system/ChemicalDetector.java`: the parameter of `odometer(double distance)` gains `@RoboChartType("real")`. This was the one rule-4 error that failed preflight. I had missed the annotation on the wiring class; every other `double` already had it.
- `controller/MovementController.java`: the AvoidingAgain "stuck" guard is rewritten from `!withinStuckPeriod && !advancedBeyondStuckDist` to `!(withinStuckPeriod || advancedBeyondStuckDist)`. The two are logically equal by De Morgan, so behaviour does not change. The new form states the negation of the preceding "progress" guard syntactically, which clears the rule-6 warning.

`result_codegen.json` was regenerated; no element changed. The build is clean and the scratch behavioural harness still passes.

**Decision — rule-8 warnings left in place on purpose:**
- Preflight warns that in AvoidingAgain the stop/resume branches precede the triggerless progress/stuck branches. RoboChart cannot express "event absent", so all four exits are enabled together in the model.
- The requirements cause this: CD-MV-Beh17–20 give AvoidingAgain both event-triggered and guard-only exits.
- Moving the triggerless branches first would make the Java stop/resume branches unreachable, since those guards cover every case. That would draw priority-negation warnings on them instead.
- The codegen rules tell me to put high-priority events first. The Java is therefore a deterministic refinement of the extracted, nondeterministic model.
- In the composed system the question does not arise: `ChemicalDetector` settles movement before gas analysis emits anything, so stop/resume never reach AvoidingAgain.

**Unresolved:** unchanged from iteration 1. See `post_codegen.md`.
