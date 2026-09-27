# LRE run 5 — iteration 2

Iteration 1 failed preflight, m2t, fdr4, dafny_verify, isabelle_verify, vacuity.
Three edits, all in my Java.

## 1. Name collision between an operation field and a Sensor method

Isabelle named it exactly: `Operator: LreController.relNsVel<s> :: R`,
`Operand: closestDynamicIndex () :: Z`, `No complex coercion from "real" to
"fun"`. `CalcCPA` had a private field `relNsVel` while `Sensor` had a method
`relNsVel(int)`; the extractor made `relNsVel` a real-valued state variable and
then applied it to an index. I removed the three invented Sensor helpers
(`relNsVel`, `relEwVel`, `relSpeedSq`) and replaced them with `tcpaTo(index)` /
`cdaTo(index)`, whose bodies are not extracted, so the degenerate
zero-relative-speed case is handled by a plain `if` rather than an epsilon
constant. `CalcCPA` now holds only `cdyn`, `cda`, `tcpa`. Rule: no operation or
controller field may share a name with an argument-taking Sensor method.

This also clears the blocking vacuity signal — `minrelspeedsq` was 1e-06 in Java
and 1 in the Dafny, because fractional constants are ceiled. The constant no
longer exists. I expect the `.dfy` parse error and the empty CSP output to share
this root cause; if not, iteration 3.

## 2. Preflight errors (blocking)

`Sensor.SAFE_LARGE_DIST` lacked `@RoboChartType("real")`; annotated.
`MIN_REL_SPEED_SQ` is gone.

## 3. Branch ordering (rule8 / rule6)

Preflight: an event-triggered branch followed by a triggerless one "has no
counterpart in RoboChart ... RoboChart has no event-absence guard". I inverted
my iteration-1 priority decision: triggerless branches now come first in each
mode, and every later branch explicitly negates all preceding triggerless
guards, so the extracted preconditions are mutually exclusive and their
disjunction is `true` in each mode (which should keep it deadlock-free).

**Cost of this, for review:** Beh6/Beh7/Beh12/Beh15 read as unconditional
operator transitions, but they are now preempted when an autonomous safety guard
holds. That priority is not representable in the extracted model, so the spec and
the model cannot both be honoured here.

Vacuity's other four signals are marked ADVISORY / no action (CSP domain
narrowing makes `odist > 1` unsatisfiable, so only OCM is reachable).
