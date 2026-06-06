# Phase 2 — Interactive Code Generation (iter 2, feedback-driven)

**Status:** complete

Iter-2 fixes driven by iter-1 feedback:

1. **preflight (21 lints)** — added `@RoboChartType("real")` to every flagged
   double field/parameter: `Actuator.lastAdvVel/.lastAdvHdng`, the four
   `LreConstants` thresholds, all six `Obstacle` record components,
   `Sensor.NO_OBSTACLE_DIST/depth/nsVel/ewVel/rateOfClimb` and the four
   `Sensor.update(...)` parameters.

2. **dafny_verify (3 unproved postconditions)** — the generator emits one
   unconditional `ensures <guard> ==> mode' == <target>` per autonomous branch;
   event-triggered branches (EndTask, ReqHCM) and earlier autonomous branches
   (inOpez) preempted them. Fix: added a `Tick` input event and gated every
   autonomous branch on `event instanceof InputEvent.Tick` plus explicit
   cross-target exclusions (`!inOpez`, `!collisionCourse`), making each ensures
   premise event-specific and mutually exclusive with differently-targeted
   branches. New named predicate `collisionCourse = cdaBelowMinSafe &&
   tcpaNonNegative`.

3. **isabelle_verify (deadlock_free timeout)** — tooling fix, not Java. Scratch
   diagnosis: with the closer `sorry`'d the theory builds in 76s, so the hog was
   the closer; the residual after `apply deadlock_free` is param-free (scalar
   payload sets are `[simp] = UNIV`), `by (metis St.exhaust_disc)` closes it in
   ~88s while the template-selected `using St.exhaust_disc by auto` exceeds the
   600s per-goal timeout on LRE's real-arithmetic guards.
   `thy_generation_rule.egl` now keys the auto closer on **Seq** payload
   domains only (preserving the documented gas-analysis regression); scalar
   payload domains get metis.

## Items for user review

- **design_choice — Tick event (not in LRE-DM6).** Scheduler artefact added to
  drive autonomous transitions; documented spec deviation (HOWTO-sanctioned
  pattern).
- **design_choice — priority-encoding negations.** Java behaviour unchanged;
  the extracted model gains explicit priority on same-mode autonomous branches.
- **design_choice — template closer fix.** Pipeline-side change, verified in a
  scratch Isabelle session before patching.

**Next step:** run the deterministic pipeline; expect preflight, dafny_verify,
isabelle_verify to be re-judged.
