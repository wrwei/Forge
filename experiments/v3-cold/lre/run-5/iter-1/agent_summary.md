# ITER_SUMMARY — lre, pass 5, iteration 1

**Written.** 15 Java files in package `lre`:
- the annotation and constants (four thresholds, all 1.0);
- the `LreMode` enum and the sealed input/output events;
- `Obstacle`, `ObstacleRegister` and `Sensor`;
- `Actuator.apply(OutputEvent)`;
- the five operation classes;
- `LreController`.

`step()` calls the five `compute()` methods, declares named predicates, then runs a four-mode if-else with 18 transitions. MOM entries advise velocity 1, HCM entries 0. EndTask (Beh7) and the CAM safe exit (Beh18) advise 0. The trace covers 51/51 requirements and all 15 files. Layer 5's `post_codegen.{md,json}` were written. No Java existed beforehand.

**Decisions the requirements left open:**
- **No-obstacle index:** the requirements use `-1`, but `cstc`/`cdyn` are natural numbers. Index 0 now means "none"; register identifiers start at 1.
- **Defaults:** safe large distance 1,000,000 m; ties go to the lowest index; the actuator starts at 0.
- **Variables:** fields of their operations, read through getters. CheckOPEZ and CalcCPA call the sensor's selection function themselves, because `compute()` may use only sensor calls and the class's own fields.
- **CPA:** horizontal, using the AUV's velocity relative to the obstacle; `cda` includes `vdist`, so no dynamic obstacle gives a large `cda`.
- **Transition order:** reqOCM, OPEZ, CAM, then HCM (Beh9, Beh11, Beh10), then endTask/reqHCM. Beh10 before Beh11 would make Beh11's guard unsatisfiable.
- **Literals:** HCM advises 0 m/s (FR3), not the description's 0.1 m/s. Beh4's thresholds stay the literal `1`.

**Unresolved:**
- At exactly zero relative horizontal velocity, `tcpa` and `cda` are NaN: CAM is never entered and is always left.
- No epsilon was added (it would put fractional literals in the model), so the model's division can have a zero divisor.
- CAM issues no evasive output; FR4 specifies none.
- MOM↔HCM cannot cycle without an event only while `staticObsDfltVertDist <= staticObsVertDist`.
