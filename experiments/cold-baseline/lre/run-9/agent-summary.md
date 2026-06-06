# LRE cold baseline run-9 — agent summary

## Design decisions

- **Package layout**: `lre.{annotation, mode, constants, event, sensor, actuator, operation, controller}` as prescribed.
- **Sealed event interfaces**: `InputEvent` with `ReqVel`, `ReqHdng`, `ReqOCM`, `ReqMOM`, `ReqHCM`, `EndTask`; `OutputEvent` with only `AdvVel`, `AdvHdng` (per LRE-DM7).
- **Sensor sentinel handling**: `hdist`/`vdist`/`odist` return `Double.MAX_VALUE` and field accessors return `0.0` when index is `-1`, satisfying LRE-DM5 so the controller predicates need no existence checks.
- **Operations**: One class per LRE-OP requirement. `CheckOPEZ` reads `cstc` via `CalcCStc` (not directly from the sensor) per LRE-OP2. `CalcCPA.compute()` keeps to exactly two field assignments per LRE-OP5 — at the cost of a long inlined expression, since no local intermediates are permitted by the codegen rules.
- **Controller**: Mode-nested if-else over `currentMode == X`. Named boolean predicates are declared once before the dispatch; simple comparisons only, no ternaries, no sentinel-existence predicates. Event-triggered transitions appear first inside each mode block, so each non-final mode has at least one bare-precondition outgoing transition (a `reqOCM` exit) — Isabelle deadlock-freedom friendly.
- **Entry actions**: `AdvVel(1.0)` on entering MOM (LRE-FR2), `AdvVel(0.0)` on entering HCM (LRE-FR3) and on `endTask`/CAM-safe transitions (LRE-Beh7, LRE-Beh18).
- **`@RoboChartType`**: `"nat"` on indices/counts, `"real"` on doubles, applied to fields, parameters, and getters.
