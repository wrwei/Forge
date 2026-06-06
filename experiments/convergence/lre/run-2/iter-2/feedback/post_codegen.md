# Post-Codegen Review — lre, iter 2

**Status:** complete. Minimal fixes driven by iter-1 feedback
(preflight + dafny_verify); no requirement scope changes.

## Changes this iter

1. **Preflight rule4:** added `@RoboChartType("real")` to all 21
   flagged double fields/parameters (Actuator, LreConstants, Obstacle
   record components, Sensor fields + updateVehicle params).
2. **Dafny postconditions:** autonomous transitions are now gated on a
   new `InputEvent.Tick` control-cycle event and carry explicit
   negations of higher-priority same-event guards (composite named
   predicates `camGuard`, `hcmHorizGuard`, `hcmVertGuard`,
   `backToMomGuard`). This makes each generated `ensures` premise
   self-contained: event-triggered branches (reqOCM/endTask/reqHCM) no
   longer silently violate autonomous postconditions, and overlapping
   autonomous guards with different target modes are now mutually
   exclusive. Branches sharing a target mode (the three MOM→HCM
   guards) are not mutually excluded — same conclusion, no conflict.

## Review items (new)

1. **design_choice — `Tick` input event added beyond LRE-DM6's list.**
   It carries no operator request; it models the control cycle so that
   autonomous guard evaluation is event-specific in the extracted
   model. Documented pattern for the Dafny premise problem.
2. **design_choice — guard negation conjuncts are redundant in Java**
   (else-if already encodes priority) but make the extracted formal
   guards mutually exclusive.

Iter-1 review items still apply.

**Next step:** re-run the pipeline.
