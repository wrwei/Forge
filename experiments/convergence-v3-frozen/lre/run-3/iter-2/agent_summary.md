# LRE run 3 — iteration 2

Iteration 1 passed 9 of 12 phases. Three failed: preflight (6 errors),
dafny_verify, vacuity (1 blocking). Both root causes were mine, and both are
about *constant declarations*, not control flow — the state machine itself came
through the extractor, FDR4 and Isabelle untouched (39 lemmas,
`LreController_deadlock_free` proved) on the first attempt.

## Fix 1 — preflight `rule4_double_missing_real_annotation` (6 errors)

I had annotated instance `double` fields but not `static final double` ones. The
linter makes no such distinction. Added `@RoboChartType("real")` to
`LreConstants.{MIN_SAFE_DIST, STATIC_OBS_HORIZ_DIST, STATIC_OBS_VERT_DIST,
STATIC_OBS_DFLT_VERT_DIST}`, `Sensor.SAFE_LARGE_DIST` and
`CalcCPA.MIN_REL_SPEED_SQ`. No behaviour change.

## Fix 2 — dafny_verify parse error + vacuity `constant_divergence`

Both trace to one literal. `CalcCPA.MIN_REL_SPEED_SQ = 1.0E-9` — the
relative-speed floor I invented in iteration 1 so `tcpa` stays finite — reached
`LreController.dfy` verbatim as `1.0E-9`. Dafny rejects exponent notation
(`LreController.dfy(13,35): this symbol not expected`), and the vacuity
comparator read the same literal back as `1`, so it reported the Dafny backend
verifying a different constant than the Java implements.

Java renders any `double` below 1e-3 in exponent form, so the value, not the
formatting, is the fix. Raised the floor to `0.001`, which `Double.toString`
emits as a plain decimal. Physically this is a floor of about 0.032 m/s on
relative speed — well below any real encounter and more defensible than 1e-9,
and it perturbs genuine `tcpa` values by about 0.1%. The .rct/CSP ceiling will
still round it to 1; that divergence is CSP-only and exactly the ceiling, which
the audit classes as advisory.

## Unresolved

The two spec ambiguities from iteration 1 stand and need a human ruling: HCM
entry velocity (LRE-FR3 says 0 m/s, the system description says 0.1 m/s), and
CAM specifying no output on entry. Neither blocks any phase.
