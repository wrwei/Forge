# ITER_SUMMARY — lre, run 2

## Iteration 1

No Java was present at the start, so nothing was deleted.

**What I wrote.** I wrote 15 files under `lre`: the events, `Obstacle`, `ObstacleRegister`, `Sensor` (SF1–3, DM5), `Actuator`, `LreConstants`, and the operations OP1–5, whose getters are named after Var1–8. `LreController.step` runs the operations, declares the named predicates (`inOpez` among them) and then a two-level if-else over the four modes. In MOM, HCM and CAM, `reqOCM` is the first, unguarded branch. Every predicate-only branch is negated in the branches below it.

**Decisions the requirements left open:**
- **No-obstacle index.** "-1 = none" conflicts with nat. The sentinel is index 0 (`NO_OBSTACLE`), and register keys are ≥ 1.
- **CPA with no obstacle.** DM5's zero field defaults would give cda = 0 (a false CAM). CalcCPA therefore takes the range from `hdist`, which carries the large default. It works in the horizontal plane with relative velocity, and adds `CPA_EPSILON` to the divisor.
- **HCM speed.** I advise 0 m/s (FR3), not the description's 0.1.
- **Literal 1s.** Velocities use `NORMAL_SPEED`; Beh4's distances use `MIN_SAFE_DIST`.
- **Priority in MOM.** reqOCM, inOpez, CAM, endTask, reqHCM, then Beh9, Beh11, Beh10. Beh11 comes before Beh10 so that it is not dead code.
- **`vvel`.** It is the signed rate of climb, as written.
- **CheckOPEZ.** It reads the closest static index from the sensor, as the profile requires.
- **No input this cycle.** `step(null)` means no operator input.
- **Invented defaults.** The safe distance is 10^6 m. Readings and advice start at 0. Ties go to the lowest index.

**Unresolved:**
- CAM's evasive manoeuvre is unspecified, so CAM has no entry action.
- MOM and HCM could oscillate if `staticObsDfltVertDist > staticObsVertDist`. The guards are exclusive at the given defaults.

Details are in `post_codegen.md`.
