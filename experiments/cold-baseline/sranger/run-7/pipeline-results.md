# Cold-baseline pipeline results — sranger run-7

Per-phase outcomes from one cold-baseline pipeline pass. The cold
codegen Java source under `src/` was copied into
`java.generated.project/src/main/java/sranger/`, then
`scripts/run_experiment_iteration.py` was invoked once.

## Phase outcomes

| Phase | Status | Time |
|---|---|---|

## Pipeline exit code

`-1`

## Stdout (last 80 lines)

```
  csp-gen\defs\function_toolkit_defs.csp (1.6 KB)
  csp-gen\defs\relation_toolkit_defs.csp (2.7 KB)
  csp-gen\defs\robochart_defs.csp (1.1 KB)
  csp-gen\defs\sequence_toolkit_defs.csp (4.2 KB)
  csp-gen\defs\set_toolkit_defs.csp (1.3 KB)
  csp-gen\defs\SRangerController.csp (20.2 KB)
  csp-gen\defs\SRangerController_coreassertions.csp (0.8 KB)
  csp-gen\defs\SRangerController_Ctrl.csp (28.7 KB)
  csp-gen\defs\SRangerController_Ctrl_coreassertions.csp (1.3 KB)
  csp-gen\defs\SRangerController_Module.csp (38.9 KB)
  csp-gen\defs\SRangerController_Module_coreassertions.csp (1.4 KB)
  csp-gen\defs\state_defs.csp (0.8 KB)
  csp-gen\file_robochart_controller.csp (0.4 KB)
  csp-gen\file_robochart_controller_coreassertions.csp (2.6 KB)
  csp-gen\instantiations.csp (1.3 KB)
  csp-gen\timed\defs\core_defs.csp (1.1 KB)
  csp-gen\timed\defs\core_timed_defs.csp (6.7 KB)
  csp-gen\timed\defs\file_robochart_controller.csp (122.3 KB)
  csp-gen\timed\defs\file_robochart_controller_defs.csp (0.3 KB)
  csp-gen\timed\defs\function_toolkit_defs.csp (1.6 KB)
  csp-gen\timed\defs\relation_toolkit_defs.csp (2.7 KB)
  csp-gen\timed\defs\robochart_defs.csp (1.1 KB)
  csp-gen\timed\defs\sequence_toolkit_defs.csp (4.2 KB)
  csp-gen\timed\defs\set_toolkit_defs.csp (1.3 KB)
  csp-gen\timed\defs\SRangerController.csp (30.8 KB)
  csp-gen\timed\defs\SRangerController_coreassertions.csp (1.1 KB)
  csp-gen\timed\defs\SRangerController_Ctrl.csp (40.4 KB)
  csp-gen\timed\defs\SRangerController_Ctrl_coreassertions.csp (1.9 KB)
  csp-gen\timed\defs\SRangerController_Module.csp (51.5 KB)
  csp-gen\timed\defs\SRangerController_Module_coreassertions.csp (2.0 KB)
  csp-gen\timed\defs\state_defs.csp (0.8 KB)
  csp-gen\timed\defs\state_timed_defs.csp (1.3 KB)
  csp-gen\timed\file_robochart_controller.csp (0.5 KB)
  csp-gen\timed\file_robochart_controller_coreassertions.csp (3.9 KB)
  csp-gen\timed\instantiations.csp (1.3 KB)
  discovered_model.xmi (103.3 KB)
  isabelle\ROOT (0.1 KB)
  isabelle\SRangerController_Beh.thy (7.2 KB)
  lint_report.json (0.0 KB)
  robochart_controller.rct (2.1 KB)
  robochart_model.xmi (4.1 KB)
  SRangerController.dfy (2.7 KB)
  trace_dafny.json (3.6 KB)
  trace_full.json (4.1 KB)
  trace_m2m.json (4.4 KB)
  trace_m2t_rct.json (2.1 KB)
  trace_t2m.json (13.8 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_gen.md
  -> phase_complete: status=completed
===== dafny_gen: completed (2.8s) =====

===== isabelle_gen =====
  [sys] 5c — Isabelle Theory Generation starting...
  [sys] Pre-cleaned 2 stale output entries for isabelle_gen.
  [sys] 5c — Isabelle Theory Generation completed successfully.
  [sys] Output (2 file(s) in isabelle/):
  ROOT (0.1 KB)
  SRangerController_Beh.thy (7.2 KB)
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_gen.md
  -> phase_complete: status=completed
===== isabelle_gen: completed (2.5s) =====

===== fdr4 =====
  [pre-fdr4] applied csp corrections to instantiations.csp
  [sys] FDR4: auto-discovered CSP file SRangerController_Module_coreassertions.csp
  [sys] FDR4: 8 assertion(s) checked, 4 passed, 2 failed, 2 expected failure(s), 0 error(s)
  [sys] Feedback written: pipeline.assets/corrections/post_fdr4.md
  -> phase_complete: status=failed
===== fdr4: failed (1.0s) =====

===== dafny_verify =====
  [sys] Verifying SRangerController.dfy...
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_verify.md
  [sys] 6b — Dafny Verification failed:
SRangerController.dfy: t2m.transformation.java/output/SRangerController.dfy(71,30): Error: a postcondition could not be proved on this return p
  -> phase_complete: status=failed
===== dafny_verify: failed (0.7s) =====

===== isabelle_verify =====
  [sys] 6c — Isabelle Verification (Z-Machine): starting (this can take minutes — first run cold loads the Z_Machine heap)...
```

## Stderr

```

[baseline] TIMEOUT after 420s — pipeline killed.
```
