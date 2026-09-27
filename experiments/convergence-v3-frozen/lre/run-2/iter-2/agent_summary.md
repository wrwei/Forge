# LRE run 2 — iteration 2

## What I changed and why

Two edits, both in the leaf layer; the controller and the state machine are
untouched. Iteration 1 passed compile, coverage, preflight, T2M and M2M, and
the generated `.rct` was already a faithful model — the four downstream
failures reduced to two defects in `Sensor`/`CalcCPA`.

1. **`relSpeedSq` was both a RoboChart `function` and a
   `LreController_State` variable.** `Sensor.relSpeedSq(index)` became
   `function relSpeedSq(index : int) : real`, while `CalcCPA`'s field of the
   same name became `var relSpeedSq : real`. The call `relSpeedSq(cdyn)` then
   resolved to a function-typed *variable* application — the CSP-generator
   crash CLAUDE.md documents, and the same clash Isabelle reported as
   `Operator: LreController.relSpeedSq<s> :: ℝ` / "No complex coercion from
   real to fun" at `LreController_Beh.thy:108`. **Fix:** deleted the
   `relSpeedSq` field from `CalcCPA` and inlined
   `sensor.relSpeedSq(calcCDyn.cdyn())` into the `tcpa` expression. This
   accounted for both the M2T and Isabelle failures (FDR4 had no verdict
   because M2T produced no CSP).

2. **`Sensor`'s two `static final` fields leaked into the model.** They were
   lifted into the `Sensors` interface as spurious state variables and into
   Dafny as `const`s, where `MIN_REL_SPEED_SQ = 1.0E-6` is not Dafny syntax
   (`LreController.dfy(13,35): this symbol not expected`) and its value
   rounded to `1`, which was the one **blocking** vacuity signal
   (`constant_divergence`, Java `1e-06` vs Dafny `1`). **Fix:** removed both
   fields and inlined their literals into the `Sensor` method bodies, which
   the extractor does not read; wrote the floor as `0.000001` rather than
   scientific notation.

## Judgement calls

None new. The iteration-1 decisions (LRE-DM5 vs LRE-OP5 defaults, the CPA
formulas, LRE-FR3's 0 m/s, MOM branch order) stand unchanged and are recorded
in `post_codegen.md`.

## Unresolved

The four *advisory* vacuity findings — only OCM reachable under the narrowed
CSP domains, `(OCM, {reqMOM})` dead at `core_real = {0..1}` — are the declared
FDR4 tractability narrowing, not something the Java can fix.
