# Cold-baseline pipeline results — chemical_detector run-9

Per-phase outcomes from one cold-baseline pipeline pass. The cold
codegen Java source under `src/` was copied into
`java.generated.project/src/main/java/chemical_detector/`, then
`scripts/run_experiment_iteration.py` was invoked once.

## Phase outcomes

| Phase | Status | Time |
|---|---|---|
| compile | failed | 2.0s |
| coverage | failed | 0.0s |
| preflight | failed | 2.8s |
| t2m | failed | 2.8s |
| m2m | failed | 2.7s |
| m2t | failed | 2.8s |
| dafny_gen | failed | 2.8s |
| isabelle_gen | failed | 2.8s |
| fdr4 | failed | 0.0s |
| dafny_verify | failed | 0.0s |
| isabelle_verify | failed | 2.7s |
| vacuity | passed | 0.0s |

## Pipeline exit code

`1`

## Stdout (last 80 lines)

```
3 actionable tasks: 1 executed, 2 up-to-date
  [sys] Feedback written: pipeline.assets/corrections/post_m2t.md
  -> phase_complete: status=failed
===== m2t: failed (2.8s) =====

===== dafny_gen =====
  [sys] 5b — Dafny Generation starting...
  [sys] 5b — Dafny Generation failed with exit code 1.
> Task :run FAILED
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x0000000403800000, 1073741824, 0) failed; error='The paging file is too small for this operation to complete' (DOS error/errno=1455)
#
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 1073741824 bytes for G1 virtual space
# An error report file with more information is saved as:
# C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\hs_err_pid33276.log
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':run'.
* Try:
BUILD FAILED in 2s
3 actionable tasks: 1 executed, 2 up-to-date
  [sys] Feedback written: pipeline.assets/corrections/post_dafny_gen.md
  -> phase_complete: status=failed
===== dafny_gen: failed (2.8s) =====

===== isabelle_gen =====
  [sys] 5c — Isabelle Theory Generation starting...
  [sys] 5c — Isabelle Theory Generation failed with exit code 1.
> Task :run FAILED
Java HotSpot(TM) 64-Bit Server VM warning: #
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 1073741824 bytes for G1 virtual space
INFO: os::commit_memory(0x0000000403800000, 1073741824, 0) failed; error='The paging file is too small for this operation to complete' (DOS error/errno=1455)
# An error report file with more information is saved as:
# C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\hs_err_pid35932.log
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':run'.
* Try:
BUILD FAILED in 2s
3 actionable tasks: 1 executed, 2 up-to-date
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_gen.md
  -> phase_complete: status=failed
===== isabelle_gen: failed (2.8s) =====

===== fdr4 =====
  [pre-fdr4] no csp corrections applied (file missing?)
  [sys] FDR4 CSP file not found. Auto-discovery looked under C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\output\csp-gen\defs for *_System_Module_coreassertions.csp, *_Module_coreassertions.csp, or *_coreassertions.csp; nothing matched. Either the m2t phase did not produce csp-gen output, or pin a path explicitly in phases.fdr4.csp_file.
  -> phase_complete: status=failed
===== fdr4: failed (0.0s) =====

===== dafny_verify =====
  [sys] No .dfy files found in output
  -> phase_complete: status=failed
===== dafny_verify: failed (0.0s) =====

===== isabelle_verify =====
  [sys] 6c — Isabelle Verification (Z-Machine): starting (this can take minutes — first run cold loads the Z_Machine heap)...
  [sys] 6c — Isabelle Verification (Z-Machine) failed — see Feedback tab.
  [sys] Feedback written: pipeline.assets/corrections/post_isabelle_verify.md
  -> phase_complete: status=failed
===== isabelle_verify: failed (2.7s) =====

===== vacuity =====
  Vacuity audit: passed (0 finding(s))
===== vacuity: passed =====

===== SUMMARY =====
  compile           failed          2.0s
  coverage          failed          0.0s
  preflight         failed          2.8s
  t2m               failed          2.8s
  m2m               failed          2.7s
  m2t               failed          2.8s
  dafny_gen         failed          2.8s
  isabelle_gen      failed          2.8s
  fdr4              failed          0.0s
  dafny_verify      failed          0.0s
  isabelle_verify   failed          2.7s
  vacuity           passed          0.0s
```

## Stderr

```
instantiations.csp not found
```
