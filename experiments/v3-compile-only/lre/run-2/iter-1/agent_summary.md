# LRE, run 2, iteration 1

## What I wrote

15 files under `lre/`:
- `LreController.step()`: the five operations run first, then named predicates, then one block per mode (OCM, MOM, HCM, CAM).
- Operations CalcVel, CalcCStc, CalcCDyn, CheckOPEZ and CalcCPA.
- `Sensor` (the DM5 functions), with `Obstacle` and `ObstacleRegister`.
- `Actuator.send(OutputEvent)`, the sealed input/output events, `LreMode`, `LreConstants` and the annotation.

Entry actions: MOM advVel(1), HCM advVel(0). Beh7 and Beh18 also send advVel(0). The trace covers all 51 requirements and every file.

## Decisions the requirements did not settle

- **"None" = -1 for natural indices:** used a reserved natural index (2147483647) that the register refuses.
- **DM5 vs OP5 defaults:** zero fields would give cda = 0, a false collision. cda = sqrt(hdist² − tcpa·(p·w)) is exact with an obstacle and large without one.
- **Invented values:** safe large distance 1e6 m. CPA_EPSILON 1e-6 keeps tcpa defined at zero relative speed; the model rounds it up to 1.
- **CPA plane:** horizontal only, since only horizontal accessors exist.
- **Shared constants:** "1 m/s" values share MOM_VEL. Beh4's "> 1" uses minSafeDist.
- **Priority:** reqOCM, inOpez and collision come first in MOM and HCM. Beh11 goes before Beh10, because after it, with equal thresholds, Beh11 could never fire.
- **Operation inputs:** CheckOPEZ and CalcCPA query the sensor's closest index directly.
- **Unhandled events:** `step(null)` means no operator input; events a mode does not list are ignored.
- **HCM speed:** FR3 (0 m/s) followed over the description's 0.1 m/s.

## Unresolved

- **CAM evasive advice:** unspecified, so none is issued.
- **CAM exit:** an obstacle that passed close and is receding keeps the LRE in CAM until reqOCM.
- **Beh11:** it uses the signed rate of climb, so it does not fire on a fast descent.

Details: `post_codegen.md`.
