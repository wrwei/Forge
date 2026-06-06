# Cold-baseline results — sranger

Each row is one cold codegen run + one pipeline pass.
Convergence = every phase reports success AND vacuity audit clean.

## Per-phase outcomes

| Run | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Timeout | Converged? |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 2 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 3 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 4 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 5 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 6 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 7 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 8 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 9 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |
| 10 | PASS | FAIL | PASS | PASS | PASS | PASS | PASS | PASS | PASS | FAIL | FAIL | PASS | yes | no |

## Convergence count

**0 of 10** cold runs converged.
