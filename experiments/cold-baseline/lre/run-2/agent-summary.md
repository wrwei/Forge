# LRE cold baseline run-2 — agent summary

## Package layout

`lre.{annotation,mode,constants,sensor,event,actuator,operation,controller}` —
one concern per sub-package. `Obstacle` and `ObstacleRegister` live alongside
`Sensor` because they are the sensor's data model.

## Key design decisions

- **Sensor sentinel handling** (LRE-DM5): all distance methods return
  `Double.MAX_VALUE` and field accessors return `0.0` when index is `-1` or
  unknown. This lets controller predicates and operations call sensor methods
  unconditionally — no sentinel guards leak into the predicate layer.
- **Operations** are concrete classes with a single `compute()` method whose
  body is only `this.field = expression` — no locals, no streams, no lambdas.
  `CheckOPEZ` depends on `CalcCStc` and `CalcCPA` depends on `CalcCDyn` by
  composition so the dependency is explicit in the model.
- **CalcCPA** computes `tcpa` first (used by `cda`) but each is a single RHS
  expression — `tcpa` is inlined into the `cda` formula via `this.tcpa`
  reference after assignment, which is allowed.
- **Controller** is a single `step(InputEvent)` method with two-level
  mode-nested if-else. All guards are extracted into named boolean predicates
  before the chain. Each non-OCM mode has at least one bare event-triggered
  transition (`ReqOCM` for MOM/HCM/CAM, `ReqVel` for OCM) to satisfy the
  Isabelle `deadlock_free` proof bare-precondition requirement.
- **Priority within MOM**: operator events (`ReqOCM`, `EndTask`, `ReqHCM`)
  precede autonomous guards (`inOpez`, CAM trigger, HCM triggers). The HCM
  triggers Beh9/Beh10/Beh11 are ordered as written in the requirements.
- **Entry actions**: MOM entry advises velocity 1.0; HCM entry advises 0.0
  (interpreting StateHCM's "reduced velocity"); CAM has no entry-action
  requirement. EndTask and CAM-exit transitions explicitly advise velocity 0.
