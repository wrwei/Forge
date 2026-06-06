# Cold-baseline pipeline results — lre run-1

Per-phase outcomes from one cold-baseline pipeline pass. The cold
codegen Java source under `src/` was copied into
`java.generated.project/src/main/java/lre/`, then
`scripts/run_experiment_iteration.py` was invoked once.

## Phase outcomes

| Phase | Status | Time |
|---|---|---|
| compile | completed | 5.4s |
| coverage | failed | 0.0s |
| preflight | completed | 2.0s |
| t2m | completed | 2.6s |
| m2m | completed | 4.0s |
| m2t | completed | 5.6s |
| dafny_gen | completed | 3.6s |
| isabelle_gen | completed | 3.1s |
| fdr4 | failed | 1.6s |
| dafny_verify | failed | 0.8s |
| isabelle_verify | completed | 59.7s |
| vacuity | passed | 0.0s |

## Pipeline exit code

`1`

## Stdout (last 80 lines)

```
  LreController.dfy (6.0 KB)
  robochart_controller.rct (6.2 KB)
  robochart_model.xmi (35.4 KB)
  trace_dafny.json (4.9 KB)
  trace_full.json (10.8 KB)
  trace_m2m.json (11.6 KB)
  trace_m2t_rct.json (4.4 KB)
  trace_t2m.json (51.2 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_gen.md
  -> phase_complete: status=completed
===== dafny_gen: completed (3.6s) =====

===== isabelle_gen =====
  [sys] 5c — Isabelle Theory Generation starting...
  [sys] Pre-cleaned 2 stale output entries for isabelle_gen.
  [sys] 5c — Isabelle Theory Generation completed successfully.
  [sys] Output (2 file(s) in isabelle/):
  LreController_Beh.thy (13.4 KB)
  ROOT (0.1 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_gen.md
  -> phase_complete: status=completed
===== isabelle_gen: completed (3.1s) =====

===== fdr4 =====
  [pre-fdr4] applied csp corrections to instantiations.csp
  [sys] FDR4: auto-discovered CSP file LreController_Module_coreassertions.csp
  [sys] FDR4: 0 assertion(s) checked, 0 passed, 1 error(s)
  [sys] ERROR: C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\output\csp-gen\defs\LreController_Module.csp:726:137-140:
    Couldn't match expected type (a, b) -> c
            with actual type (d) -> Int
    In the expression: max
    In the expression:…
    In the expression:…
    Relevant variable types:
        max :: (e) -> Int
        core_real :: {Int}
        Plus :: (Int, Int, {Int}) -> Int
        Minus :: (Int, Int, {Int}) -> Int
        Mult :: (Int, Int, {Int}) -> Int
        Div :: (Int, Int, {Int}) -> Int
        Neg :: (Int, {Int}) -> Int
        ewRelDist :: (f) -> Int
        obsNsVel :: (g) -> Int
        nsRelDist :: (h) -> Int
  [sys] Feedback written: pipeline.assets/corrections/post_fdr4.md
  -> phase_complete: status=failed
===== fdr4: failed (1.6s) =====

===== dafny_verify =====
  [sys] Verifying LreController.dfy...
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_verify.md
  [sys] 6b — Dafny Verification failed:
LreController.dfy: t2m.transformation.java/output/LreController.dfy(106,35): Error: a postcondition could not be proved on this return path
  -> phase_complete: status=failed
===== dafny_verify: failed (0.8s) =====

===== isabelle_verify =====
  [sys] 6c — Isabelle Verification (Z-Machine): starting (this can take minutes — first run cold loads the Z_Machine heap)...
  [sys] 6c — Isabelle Verification (Z-Machine) — 1 theory; 20 lemmas verified; deadlock-freedom proved for `LreController_deadlock_free`; elapsed 0:00:53.
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_verify.md
  -> phase_complete: status=completed
===== isabelle_verify: completed (59.7s) =====

===== vacuity =====
  Vacuity audit: passed (0 finding(s))
===== vacuity: passed =====

===== SUMMARY =====
  compile           completed       5.4s
  coverage          failed          0.0s
  preflight         completed       2.0s
  t2m               completed       2.6s
  m2m               completed       4.0s
  m2t               completed       5.6s
  dafny_gen         completed       3.6s
  isabelle_gen      completed       3.1s
  fdr4              failed          1.6s
  dafny_verify      failed          0.8s
  isabelle_verify   completed      59.7s
  vacuity           passed          0.0s
```
