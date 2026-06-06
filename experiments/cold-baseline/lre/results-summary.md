# Cold-baseline results — lre

Each row is one cold codegen run + one pipeline pass.
Convergence = every phase reports success AND vacuity audit clean.

## Per-phase outcomes

| Run | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Timeout | Converged? |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 2 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 3 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 4 | PASS | FAIL | PASS | PASS | PASS | FAIL | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 5 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 6 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 7 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 8 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 9 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |
| 10 | FAIL | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | PASS | PASS | no | no |

## Convergence count

**0 of 10** cold runs converged.
