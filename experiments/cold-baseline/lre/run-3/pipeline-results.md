# Cold-baseline pipeline results — lre run-3

Per-phase outcomes from one cold-baseline pipeline pass. The cold
codegen Java source under `src/` was copied into
`java.generated.project/src/main/java/lre/`, then
`scripts/run_experiment_iteration.py` was invoked once.

## Phase outcomes

| Phase | Status | Time |
|---|---|---|
| compile | completed | 5.6s |
| coverage | failed | 0.0s |
| preflight | completed | 2.1s |
| t2m | completed | 2.6s |
| m2m | completed | 3.9s |
| m2t | completed | 5.4s |
| dafny_gen | completed | 3.7s |
| isabelle_gen | completed | 2.8s |
| fdr4 | completed | 226.7s |
| dafny_verify | failed | 0.7s |
| isabelle_verify | completed | 52.3s |
| vacuity | passed | 0.0s |

## Pipeline exit code

`1`

## Stdout (last 80 lines)

```
  csp-gen\timed\defs\LreController_Ctrl_coreassertions.csp (2.4 KB)
  csp-gen\timed\defs\LreController_Module.csp (194.3 KB)
  csp-gen\timed\defs\LreController_Module_coreassertions.csp (2.7 KB)
  csp-gen\timed\defs\relation_toolkit_defs.csp (2.7 KB)
  csp-gen\timed\defs\robochart_defs.csp (1.1 KB)
  csp-gen\timed\defs\sequence_toolkit_defs.csp (4.2 KB)
  csp-gen\timed\defs\set_toolkit_defs.csp (1.3 KB)
  csp-gen\timed\defs\state_defs.csp (0.8 KB)
  csp-gen\timed\defs\state_timed_defs.csp (1.3 KB)
  csp-gen\timed\file_robochart_controller.csp (0.5 KB)
  csp-gen\timed\file_robochart_controller_coreassertions.csp (8.1 KB)
  csp-gen\timed\instantiations.csp (9.0 KB)
  discovered_model.xmi (522.5 KB)
  isabelle\LreController_Beh.thy (14.1 KB)
  isabelle\ROOT (0.1 KB)
  lint_report.json (0.0 KB)
  LreController.dfy (5.8 KB)
  robochart_controller.rct (6.0 KB)
  robochart_model.xmi (32.2 KB)
  trace_dafny.json (7.7 KB)
  trace_full.json (10.8 KB)
  trace_m2m.json (11.6 KB)
  trace_m2t_rct.json (4.4 KB)
  trace_t2m.json (51.4 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_gen.md
  -> phase_complete: status=completed
===== dafny_gen: completed (3.7s) =====

===== isabelle_gen =====
  [sys] 5c — Isabelle Theory Generation starting...
  [sys] Pre-cleaned 2 stale output entries for isabelle_gen.
  [sys] 5c — Isabelle Theory Generation completed successfully.
  [sys] Output (2 file(s) in isabelle/):
  LreController_Beh.thy (14.1 KB)
  ROOT (0.1 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_gen.md
  -> phase_complete: status=completed
===== isabelle_gen: completed (2.8s) =====

===== fdr4 =====
  [pre-fdr4] applied csp corrections to instantiations.csp
  [sys] FDR4: auto-discovered CSP file LreController_Module_coreassertions.csp
  [sys] FDR4: 8 assertion(s) checked, 8 passed, 0 error(s)
  [sys] Feedback written: pipeline.assets/corrections/post_fdr4.md
  [sys] 6a — Formal Verification (FDR4) completed — all assertions passed.
  -> phase_complete: status=completed
===== fdr4: completed (226.7s) =====

===== dafny_verify =====
  [sys] Verifying LreController.dfy...
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_verify.md
  [sys] 6b — Dafny Verification failed:
LreController.dfy: t2m.transformation.java/output/LreController.dfy(97,35): Error: a postcondition could not be proved on this return path
  -> phase_complete: status=failed
===== dafny_verify: failed (0.7s) =====

===== isabelle_verify =====
  [sys] 6c — Isabelle Verification (Z-Machine): starting (this can take minutes — first run cold loads the Z_Machine heap)...
  [sys] 6c — Isabelle Verification (Z-Machine) — 1 theory; 20 lemmas verified; deadlock-freedom proved for `LreController_deadlock_free`; elapsed 0:00:47.
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_verify.md
  -> phase_complete: status=completed
===== isabelle_verify: completed (52.3s) =====

===== vacuity =====
  Vacuity audit: passed (0 finding(s))
===== vacuity: passed =====

===== SUMMARY =====
  compile           completed       5.6s
  coverage          failed          0.0s
  preflight         completed       2.1s
  t2m               completed       2.6s
  m2m               completed       3.9s
  m2t               completed       5.4s
  dafny_gen         completed       3.7s
  isabelle_gen      completed       2.8s
  fdr4              completed     226.7s
  dafny_verify      failed          0.7s
  isabelle_verify   completed      52.3s
  vacuity           passed          0.0s
```
