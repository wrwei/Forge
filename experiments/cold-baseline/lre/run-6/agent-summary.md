# LRE Cold-Baseline Run 6 — Agent Summary

## Design decisions

- **Package layout** mirrors the prescribed concerns: `annotation`, `mode`,
  `event`, `constants`, `sensor`, `actuator`, `operation`, `controller`.
- **Sentinel handling** lives entirely in the `Sensor`: `odist/hdist/vdist`
  return `Double.MAX_VALUE` on idx `-1`; `nsRelDist/ewRelDist/obsNsVel/obsEwVel`
  return `0.0`. The controller therefore never guards on `cstc != -1` or
  `cdyn != -1`.
- **Operation order** in `step()` is CalcVel → CalcCStc → CalcCDyn →
  CheckOPEZ → CalcCPA, respecting the dependency that CheckOPEZ reads
  `cstc` from CalcCStc and CalcCPA reads `cdyn` from CalcCDyn.
- **CPA formulae** are inlined as a single arithmetic expression each, per
  LRE-OP5's two-assignment constraint. When `cdyn == -1` the sensor
  returns 0-valued accessors, producing `NaN` for `tcpa`/`cda`; Java NaN
  comparisons evaluate false, so the MOM→CAM / HCM→CAM guards safely
  cannot fire.
- **Bare-precondition coverage** for Isabelle deadlock-freedom: OCM has
  `reqVel`/`reqHdng` self-loops; MOM/HCM/CAM each have an unguarded
  `reqOCM` branch. No `else { currentMode = SameMode; }` self-loops are
  added — preferring the event-triggered route per CLAUDE.md guidance.
- **Reserved-word avoidance**: sensor methods use `idx` for parameters;
  controller state fields use `currentMode`, `inOpez`, `cstc`, etc.
- **Entry actions** (`AdvVel(1.0)` on MOM-entry, `AdvVel(0.0)` on
  HCM-entry / Beh7 / Beh18) are emitted inline immediately after the
  `currentMode = …` assignment in the transition branch.
