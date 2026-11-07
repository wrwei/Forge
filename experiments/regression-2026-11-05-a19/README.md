# Regression for Amendment 19 (5-6 November 2026)

Pins compared: Amendment 16 `aaddf222` against Amendment 19 `1ad9301e` (`1ad9301e00feebca5dedec8078d047a275ba45c4`).
Every recorded Java source was re-extracted at both pins (T2M, M2M, Dafny, RoboChart text,
Isabelle; `reg_a19.py`) and the generated artefacts compared after canonicalisation.

Sources: 441 in total — ablation-d2-diagnostic 27, cold-baseline (May) 30, cold-baseline-v2 15, convergence-v3-frozen 20, multicontroller-e 8, mutants 186, reference-multicontroller-f 6, reference-runs 3, v3-cold 40, v3-compile-only 17, v3-compile-only-staging 1, v3-reference 40, v3-superseded-a10 1, v3-superseded-a15 20, v3-superseded-a16 1, v3-superseded-a7 24, v3-superseded-a9 2.

## First form of the collector rule (superseded)

`regression-first-rule.json`. All 441 sources extracted at both pins. 120 differed:
the rule "skip static finals that are not numeric primitives or wrappers" also removed
record-typed constants wrapping a number (`static final Intensity thr = new Intensity(10.0)`)
that the generator unwraps and controller guards reference (200 removed consts), and 18
placeholder consts for multi-argument record constructors. The rule was narrowed before any
pin was committed (Amendment 19, correction note).

## Final rule: array-typed static finals only

`regression.json` re-extracts the 120 sources that differed under the first rule; the other
321 were unchanged under the first rule and the final rule removes a strict subset of what the
first did, so they are unchanged under it too. All 120 extract at both pins. Exactly two
differ, both the triggering iterations:

- `v3-compile-only/lre/run-1/iter-1`, `LreController_Beh.thy`: 10 lines differ only in a
  restored `()` on constant calls (`momentrymaxvel()`, `mintcpa()`).
- `v3-compile-only/chemical_detector/run-5/iter-1`, both `.dfy` files: the two lines
  `function values(): real` and `const DIRECTIONS: real := values()` removed.

No other recorded artefact changes. The two iterations are re-scored from their recorded
Java at the new pin (Amendment 19, re-scoring rule).
