# Cold-baseline pipeline results — lre run-4

Per-phase outcomes from one cold-baseline pipeline pass. The cold
codegen Java source under `src/` was copied into
`java.generated.project/src/main/java/lre/`, then
`scripts/run_experiment_iteration.py` was invoked once.

## Phase outcomes

| Phase | Status | Time |
|---|---|---|
| compile | completed | 5.4s |
| coverage | failed | 0.0s |
| preflight | completed | 1.9s |
| t2m | completed | 2.5s |
| m2m | completed | 3.8s |
| m2t | failed | 5.1s |
| dafny_gen | completed | 3.7s |
| isabelle_gen | completed | 2.9s |
| fdr4 | failed | 0.0s |
| dafny_verify | failed | 0.7s |
| isabelle_verify | completed | 54.4s |
| vacuity | passed | 0.0s |

## Pipeline exit code

`1`

## Stdout (last 80 lines)

```
  [sys] 5a — CSP Generation (RoboChart) reported errors to stdout while still exiting 0 (silent failure — gradle task ignored its own exit value).
ERROR:Couldn't resolve reference to NamedExpression 'nsRelDist'. (file:/C:/Users/willr/gitee/formal_method_guided_vibe_coding/t2m.transformation.java/output/robochart_controller.rct line : 81 column : 197)
ERROR:Couldn't resolve reference to NamedExpression 'ewRelDist'. (file:/C:/Users/willr/gitee/formal_method_guided_vibe_coding/t2m.transformation.java/output/robochart_controller.rct line : 81 column : 247)
BUILD SUCCESSFUL in 2s
1 actionable task: 1 executed
  [sys] Removed stale trace_full.json (m2t failed; downstream feedback should not read prior-run trace).
  [sys] Feedback written: pipeline.assets/corrections/post_m2t.md
  -> phase_complete: status=failed
===== m2t: failed (5.1s) =====

===== dafny_gen =====
  [sys] 5b — Dafny Generation starting...
  [sys] Pre-cleaned 1 stale output entry for dafny_gen.
  [sys] 5b — Dafny Generation completed successfully.
  [sys] Output (12 file(s) in output/):
  constant_defaults.json (0.1 KB)
  discovered_model.xmi (543.1 KB)
  isabelle\LreController_Beh.thy (14.1 KB)
  isabelle\ROOT (0.1 KB)
  lint_report.json (0.0 KB)
  LreController.dfy (6.1 KB)
  robochart_controller.rct (5.9 KB)
  robochart_model.xmi (32.8 KB)
  trace_dafny.json (7.7 KB)
  trace_m2m.json (11.6 KB)
  trace_m2t_rct.json (4.4 KB)
  trace_t2m.json (55.4 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_gen.md
  -> phase_complete: status=completed
===== dafny_gen: completed (3.7s) =====

===== isabelle_gen =====
  [sys] 5c — Isabelle Theory Generation starting...
  [sys] Pre-cleaned 2 stale output entries for isabelle_gen.
  [sys] 5c — Isabelle Theory Generation completed successfully.
  [sys] Output (2 file(s) in isabelle/):
  LreController_Beh.thy (14.0 KB)
  ROOT (0.1 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_gen.md
  -> phase_complete: status=completed
===== isabelle_gen: completed (2.9s) =====

===== fdr4 =====
  [pre-fdr4] no csp corrections applied (file missing?)
  [sys] FDR4 CSP file not found. Auto-discovery looked under C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\output\csp-gen\defs for *_System_Module_coreassertions.csp, *_Module_coreassertions.csp, or *_coreassertions.csp; nothing matched. Either the m2t phase did not produce csp-gen output, or pin a path explicitly in phases.fdr4.csp_file.
  -> phase_complete: status=failed
===== fdr4: failed (0.0s) =====

===== dafny_verify =====
  [sys] Verifying LreController.dfy...
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_verify.md
  [sys] 6b — Dafny Verification failed:
LreController.dfy: t2m.transformation.java/output/LreController.dfy(105,35): Error: a postcondition could not be proved on this return path
  -> phase_complete: status=failed
===== dafny_verify: failed (0.7s) =====

===== isabelle_verify =====
  [sys] 6c — Isabelle Verification (Z-Machine): starting (this can take minutes — first run cold loads the Z_Machine heap)...
  [sys] 6c — Isabelle Verification (Z-Machine) — 1 theory; 20 lemmas verified; deadlock-freedom proved for `LreController_deadlock_free`; elapsed 0:00:49.
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_verify.md
  -> phase_complete: status=completed
===== isabelle_verify: completed (54.4s) =====

===== vacuity =====
  Vacuity audit: passed (0 finding(s))
===== vacuity: passed =====

===== SUMMARY =====
  compile           completed       5.4s
  coverage          failed          0.0s
  preflight         completed       1.9s
  t2m               completed       2.5s
  m2m               completed       3.8s
  m2t               failed          5.1s
  dafny_gen         completed       3.7s
  isabelle_gen      completed       2.9s
  fdr4              failed          0.0s
  dafny_verify      failed          0.7s
  isabelle_verify   completed      54.4s
  vacuity           passed          0.0s
```

## Stderr

```
instantiations.csp not found
```
