# ITER_SUMMARY — lre, run 3, iteration 1

## What was written
The work followed the five layers in order. There are 15 files under `lre/`:
- the `RoboChartType` annotation and `LreConstants` (the four DM4 thresholds, plus named constants for the other fixed values);
- the `LreMode` enum, with OCM first;
- the `InputEvent` and `OutputEvent` sealed interfaces;
- `Obstacle`, `ObstacleRegister` and `Sensor`;
- `Actuator.apply(OutputEvent)`;
- the five operations;
- `LreController.step()`, which runs the operations, declares 17 named predicates, then takes one branch of a two-level if-else per Beh2–Beh18.

`result_codegen.json` traces all 51 requirements.

## Decisions the requirements did not settle
- **"None" index.** cstc/cdyn are nat but "none" is −1. "None" is index 2147483647, which the register never stores. Distances for it are 1000000.0 m (invented); field accessors return 0.
- **CPA.** p = obstacle position relative to the AUV, w = AUV velocity − obstacle velocity. tcpa = p·w/(|w|²+ε), with ε = 0.000001 (the model rounds it to 1; the sign of tcpa is unaffected). cda combines the horizontal miss distance with vdist. With no dynamic obstacle, cda is therefore large.
- **HCM velocity.** Advised velocity is 0 (FR3), not the system description's 0.1.
- **vvel** is the signed rate of climb.
- **Priority within modes.** Operator overrides (reqOCM, endTask) come first, then OPEZ, collision course, then the HCM triggers, with reqHCM last. Lower guard-only branches conjoin the negations of the guards above them. Beh11 is placed before Beh10 because, with equal thresholds, Beh10 placed first would leave Beh11's guard unsatisfiable.
- **Variables** live in the operations. CheckOPEZ and CalcCPA re-read the closest index from the sensor.
- **Literal 1s** in Beh4/9/11 became separate named constants.

## Unresolved
- CAM's evasive manoeuvre is unspecified, so entering CAM issues no advice.

All of these are listed in `post_codegen.md`.
