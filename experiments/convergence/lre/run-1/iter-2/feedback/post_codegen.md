# Post-Codegen Review — lre, run-4 iter-2

**Status:** complete

**Summary (iter-2 changes):** Minimal fixes driven by iter-1 feedback:
(a) preflight — added the 5 missing `@RoboChartType("real")` annotations
(`Sensor.updateEnvironment` params, `Sensor.SAFE_LARGE_DISTANCE`);
(b) dafny_verify — introduced an `InputEvent.tick` autonomous-evaluation
event and gated all guard-only transitions on it, and made same-mode
autonomous guards mutually exclusive by conjoining negations of
higher-priority guards (`!inOpez`, `!collisionImminent`) where targets
differ. New named predicate `collisionImminent = cda < minSafeDist &&
tcpa >= 0.0` (defined directly from fields, not from other predicates,
in case generator predicate-inlining is single-level); `tcpaNonNegative`
removed (subsumed).

**Iter-2 issues:**

1. **design_choice — tick gating of autonomous transitions.** The Dafny
   generator emits `ensures <guard> ==> mode == <target>` unconditionally
   for guard-only branches, which operator-event branches preempt. Gating
   on `tick` makes the premise event-specific. Deviation risk vs LRE-DC1
   noted: several transitions now share the (mode, tick) trigger pair,
   distinguished by mutually exclusive guards; each Beh requirement still
   maps to exactly one transition.

2. **design_choice — explicit priority negations.** Beh13/Beh8/Beh14
   guards now carry the negation of higher-priority same-mode guards
   (semantically identical to the Java else-if priority, encoded
   explicitly so each transition's formal contract is self-contained).

---

# (iter-1 review, retained) Post-Codegen Review — lre, run-4 iter-1 (cold codegen)

**Status:** complete

**Summary:** Full cold codegen of the LRE safety controller from
`requirement_all.json` (43 requirements). 13 Java files under
`java.generated.project/src/main/java/lre/`. All requirements traced in
`result_codegen.json`. The issues below are design choices and invented
defaults the user should review.

## Issues

1. **design_choice — lowercase event record names.** Records are named
   `reqVel`, `advVel`, etc. (not Java-conventional `ReqVel`) so the
   extracted RoboChart event names match the requirement spec exactly
   (the EGL pattern-matches `actuator.apply(new OutputEvent.advVel(...))`).

2. **invented_default — safe large distance = 1000.0.** LRE-DM5 requires
   "a safe large distance" when no obstacle exists but gives no value.
   `Sensor.SAFE_LARGE_DISTANCE = 1000.0` (private to the sensor layer).

3. **design_choice — CPA geometry in 3D, computed in the Sensor layer.**
   LRE-OP5 does not give the CPA formulas. `Sensor.tcpaTo`/`cdaTo`
   implement the standard closest-point-of-approach equations in three
   dimensions (incl. depth difference and rate-of-climb, since LRE-DM2
   carries `obs_roc`). tcpa = -(r·v)/|v|² with 0 returned for zero
   relative speed; for cda the time is clamped at ≥0 so a receding
   obstacle reports its current distance. The math lives in Sensor (not
   in `CalcCPA.compute()`) because compute() may not declare locals or
   conditionals, and the division needs a zero-divisor guard.

4. **ambiguous_requirement — CAM has no entry action.** LRE-FR4 says CAM
   "performs evasive manoeuvres" but LRE-DM7 limits outputs to
   advVel/advHdng and no requirement specifies an evasive output. CAM is
   entered with no entry action; nothing is advised until CAM exits
   (Beh18 advises 0 on exit).

5. **design_choice — Beh4 thresholds taken literally.** "vel ≤ 1",
   "odist > 1" use literal 1.0 (the requirement text says "1"), not the
   `minSafeDist` constant (which happens to also be 1).

6. **design_choice — cstc/cdyn sentinel -1 kept per spec.** LRE-Var5/6
   declare the variables natural-number typed yet use -1 for "none";
   the Java fields are `@RoboChartType("nat") int` initialised to -1,
   and controller predicates call sensor functions directly without
   sentinel checks (the sensor returns safe defaults), per the codegen
   rules.

7. **design_choice — operation invocation order.** step() invokes
   CalcVel, CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA in that order so
   CheckOPEZ sees this cycle's cstc and CalcCPA this cycle's cdyn.

8. **design_choice — OCM passthrough modelled as explicit self-loops.**
   Beh2/Beh3 assign `currentMode = LreMode.OCM` redundantly so the ETL
   extracts the passthrough as an explicit self-transition with the
   advVel/advHdng action.
