# LRE run 1 — iteration 2

## What changed and why
Iteration 1 passed 10 of 12 phases. Both failures had a single root cause: the
invented constant `MIN_CLOSING_SPEED_SQ = 1.0e-9`, which I had added to
`CalcCPA` to keep the closest-point-of-approach division defined (a `compute()`
body may not branch or declare locals, so I could not guard the denominator
there).

- **dafny_verify** — `LreController.dfy(12,39)` "this symbol not expected":
  the constant is emitted as `1.0E-9`, and Dafny's real literals do not accept
  scientific notation.
- **vacuity** — `constant_divergence`: the same constant reaches the backends as
  `1` (fractional constants are ceiled), so a verifier would have checked a
  system with a denominator 10^9 times too large.

Fix: deleted the constant and moved the degenerate case into the Sensor layer,
where LRE-OP5 says safe defaults belong. New `Sensor.closingSpeedSq(index)`
returns the squared horizontal closing speed, or `1.0` when the AUV and the
obstacle move identically. Sensor methods may branch, so this is an honest
case split rather than a numerical fudge — and it is exact, not approximate:
when the closing speed is zero the CPA numerator is zero too, so `tcpa = 0` and
`cda` is the current relative distance, which is the correct answer for a
non-closing obstacle. No new constant field is introduced, so nothing new
reaches `constant_defaults.json`.

`CalcCPA.compute()` now reads `this.closingSpeedSq = sensor.closingSpeedSq(this.cdyn);`.
`./gradlew build` clean. `result_codegen.json` refreshed (66 entries; the new
method traced to LRE-OP5).

## Decisions not settled by the requirements
Unchanged from iteration 1 and still recorded in `post_codegen.{md,json}`:
HCM entry velocity 0 vs 0.1 m/s (LRE-FR3 vs the system description);
`nsRelDist`/`ewRelDist` no-obstacle default (LRE-DM5 says zero, LRE-OP5 says
large-distance — followed OP5, since zero forces a spurious permanent CAM);
`SAFE_LARGE_DISTANCE = 1000.0`; transition priority within each mode;
`ObstacleRegister` as a dense list.

## Unresolved
The vacuity advisory `dead_state_no_offer (OCM, {})` is marked ADVISORY by the
audit itself — it follows from the CSP domain narrowing, not the Java — so no
change was made for it.
