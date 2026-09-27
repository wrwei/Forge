# LRE run 2 — iteration 1 (cold codegen)

## What I wrote

`src/main/java/` was empty (scaffold only — no stale tree to delete), so all
15 Java files are new: `annotation/RoboChartType`, `mode/LreMode`,
`constants/LreConstants`, `sensor/{Obstacle, ObstacleRegister, Sensor}`,
`event/{InputEvent, OutputEvent}`, `actuator/Actuator`, `operation/{CalcVel,
CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA}`, `controller/LreController`. Plus
`result_codegen.json` (69 rows; all 51 requirements traced, every file named)
and `post_codegen.{md,json}`. `./gradlew build` is clean. `build.gradle`'s
stale `group` left alone as instructed. Event and obstacle-field identifiers
use the spec's own spelling (`reqVel`, `ns_rel_dist`) so the extracted
RoboChart names match verbatim.

## Decisions the requirements did not settle

1. **LRE-DM5 vs LRE-OP5.** DM5 says all four field accessors return zero when
   no obstacle exists; OP5 says the Sensor gives "zero-velocity and
   large-distance defaults". Zero for `nsRelDist`/`ewRelDist` models a phantom
   obstacle at the AUV's own position — `cda = 0`, spurious MOM→CAM. Resolved
   for OP5: distance accessors return 1000 m, velocity accessors zero.
2. **CPA formulas unspecified.** Used horizontal-plane `tcpa = -(r·v)/|v|²`,
   `cda = |r + v·tcpa|`. `compute()` may not branch, so
   `Sensor.relSpeedSq(index)` floors the divisor at 1e-6 to keep it total.
3. **HCM speed.** `system_description.txt` says 0.1 m/s, LRE-FR3 says 0.
   Implemented FR3 — `requirement_all.json` is canonical.
4. **MOM branch order** Beh9, Beh11, Beh10. Triggerless branches hand-carry
   predecessors' negations; Beh10 first would leave Beh11 unsatisfiable when
   both vertical thresholds equal 1.
5. **Event branches before autonomous ones**, per `java_codegen_rules.txt`;
   preflight `rule8` prefers the reverse. Expect advisory rule8 warnings.

## Not resolved

LRE-FR4 says CAM "performs evasive manoeuvres" but names no advVel/advHdng
values, so CAM has no entry action. LRE-Var5/Var6 mandate a −1 sentinel while
the M2M maps `int → nat` unconditionally; the sentinel is confined to the
Sensor layer, so no guard compares against it, but the extracted index domain
under-approximates.
