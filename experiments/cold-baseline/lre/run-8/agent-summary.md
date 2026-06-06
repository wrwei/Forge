# LRE Cold Baseline Run 8 — Agent Summary

Cold single-shot generation of the LRE safety controller against `requirement_all.json`.

## Design decisions

- **Package layout**: `lre.{annotation,actuator,constants,controller,event,mode,operation,sensor}`.
- **Modes**: enum `LreMode { OCM, MOM, HCM, CAM }`; controller initialises to OCM (LRE-Beh1).
- **Events**: sealed `InputEvent` (ReqVel, ReqHdng, ReqOCM, ReqMOM, ReqHCM, EndTask) and sealed `OutputEvent` (AdvVel, AdvHdng). Per LRE-DM7, no `setVelocity`; everything goes through `actuator.receive(...)`.
- **Sensor safe defaults**: `odist/hdist/vdist` return `Double.MAX_VALUE` for `index == -1` or missing; `nsRelDist/ewRelDist/obsNsVel/obsEwVel` return `0.0`. Per LRE-DM5 spec.
- **Operations**: one class each — `CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ` (reads cstc from CalcCStc), `CalcCPA` (reads cdyn from CalcCDyn). Each `compute()` is direct `this.field = expression` only.
- **Controller**: single `step(InputEvent event)` method. All operations invoked once at the top, then their results copied into Ctrl_State fields. Named boolean predicates declared before the pure two-level if-else. Outer chain uses `currentMode == X` only.
- **Priority within MOM**: ReqOCM, EndTask, inOpez (LRE-Beh5 — guard ONLY inOpez), CAM (collision), ReqHCM, then autonomous HCM guards.
- **Entry actions**: AdvVel(1.0) on entry to MOM (LRE-FR2); AdvVel(0.0) on entry to HCM (LRE-FR3); AdvVel(0.0) on EndTask and CAM_to_OCM_safe transitions.
- **Invented default**: small epsilon `1.0e-9` added to CPA denominator to avoid div-by-zero when relative velocity is exactly zero.
