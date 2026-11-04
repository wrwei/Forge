# ITER_SUMMARY: LRE, pass 4, iteration 1

## Written

15 Java files under `lre.*`:

- `LreController`: one `step`, four modes (OCM first), 18 transition branches over named predicates.
- Five operations holding Var1–Var8.
- `Sensor`, `ObstacleRegister`, `Obstacle` and `Actuator`.
- Sealed events, `LreConstants`, `LreMode` and `RoboChartType`.

`result_codegen.json` traces all 51 requirements and every public element. javac compiles cleanly; 44 jshell scenario checks passed.

## Open decisions and how I resolved them

1. **"No obstacle" index.** -1 is not a natural number, so index 0 (`NO_OBSTACLE`) means none; obstacle identifiers start at 1.
2. **CPA with no dynamic obstacle.** DM5 gives zero positions there, OP5 large distances. I wrote cda = sqrt(odist² − tcpa²·|w|²), so the large default decides (cda 100000, tcpa 0) instead of a false CAM entry.
3. **CPA conventions.** Positions are obstacle minus AUV, obstacle velocities absolute, motion horizontal with constant vertical separation, cda unclamped.
4. **Division by zero.** `CPA_EPSILON` = 0.000001 prevents it; the model rounds it to 1.
5. **Safe large distance.** 100000 m.
6. **HCM velocity.** 0 (FR3), not 0.1 (system description).
7. **CAM advice.** None; the manoeuvre is unspecified.
8. **Priority.** reqOCM, then collision, OPEZ, operator events, automatic HCM entries. Beh11 precedes Beh10 so it stays reachable under equal thresholds. Each lower branch requires earlier guard-only branches to be false.
9. **Closest index.** `CheckOPEZ` and `CalcCPA` read it from the sensor again, since operations take only sensors.
10. **Bare "1"s.** Beh4/9/11's thresholds became named constants.
11. **vvel.** Signed rate of climb, so Beh11 ignores fast descent.

## Unresolved

- What CAM should do.
- Whether cda should be clamped when tcpa < 0; as written, CAM persists for a receding obstacle until relative motion changes or reqOCM.
- Whether the epsilon's rounding matters.

All 13 points are in `post_codegen.{md,json}`.
