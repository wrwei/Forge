# LRE run 6 — iteration 3

## What changed and why

Iteration 2 cleared both of iteration 1's failures (Dafny parse error and the
blocking `minrelspeedsq` constant divergence) by deleting the epsilon constant
and moving the zero-relative-velocity floor into `Sensor.relSpeedSq(index)`.
That fix introduced a new one: `m2t` and `isabelle_verify` both failed.

`post_isabelle_verify.md` named the cause exactly —
`Operator: LreController.relSpeedSq :: ℝ` applied to
`Operand: closestDynamicIndex () :: ℤ`, "No complex coercion from real to fun"
at `zoperation` line 111. The extractor lifts operation-class fields into
machine state, so my `CalcCPA.relSpeedSq` **field** became a real-typed state
variable with the same name as the new `Sensor.relSpeedSq` **function**. The
call site then read as applying a real to an argument. The same collision is
what silently killed CSP generation (`m2t` produced no files) — the RoboChart
generator does not support function-typed variable application.

**Fix:** deleted the `relSpeedSq` field from `CalcCPA` and called
`sensor.relSpeedSq(cdyn)` directly at the two sites that need it. The sensor
function keeps its name; nothing in machine state shares it. I then checked
every `Sensor` public method name against every private field in the tree —
no collisions remain. Build clean.

## Decisions the requirements did not settle

Unchanged and still open for human review:

1. **LRE-DM5 is internally inconsistent** — `nsRelDist`/`ewRelDist` must return
   zero with no obstacle while `hdist` must return a safe large distance, yet
   LRE-SF1 defines `hdist` from those accessors. Distance functions got their
   own no-obstacle branch, and `cda²` was expanded as
   `hdist(cdyn)² + 2·tcpa·closingRate + tcpa²·relSpeedSq(cdyn)` so the safe
   default carries through instead of giving `cda = 0` and a false MOM→CAM.
2. `SAFE_DISTANCE = 1000.0` m is invented; LRE-DM5 says only "safe large".
3. LRE-Beh4's bare "1" reads as `MIN_SAFE_DIST` for distances, literal for `vel`.
4. CAM gets no entry action — LRE-FR4 names no output value.

## Unresolved

`cstc`/`cdyn` are natural numbers carrying a `-1` sentinel while `int` maps to
RoboChart `nat` unconditionally. Implemented as specified.

The four advisory vacuity findings are marked "no action for the codegen
agent"; no change made for them.
