# Cold baseline, replayed with the published extractor

One pipeline pass per recorded cold source. Phase 2b (coverage) is excluded from the failing-phase
column, as in the paper. A pass without verdict (driver timeout) is excluded from every count.

| Study | Passes | With verdict | Compiled (2a) | Failing phase (runs) | Converged |
|---|---:|---:|---:|---|---:|
| sranger | 10 | 10 | 10 | FDR4 6b (6) | **4** |
| lre | 10 | 10 | 9 | Isabelle 6c (1); compile 2a (1) | **8** |
| chemical_detector | 10 | 10 | 9 | Isabelle 6c (2); Dafny 6a (2); M2T 5b (2); preflight 2c (2); compile 2a (1); FDR4 6b (1) | **0** |
| **Total** | 30 | 30 | 28 | — | **12** |

## Best-of-N over the replayed cold runs

Disjoint groups in run order within each study. Oracle-free: the group converges if any run in it converged.
Verifier-informed: the group's representative is the run passing the most deciding phases; the group converges if that run converged.

| Selector | N | Groups per study | Groups in all | Converged |
|---|---:|---:|---:|---:|
| oracle-free | 2 | 5/5/5 | 15 | 9 of 15 |
| oracle-free | 3 | 3/3/3 | 9 | 6 of 9 |
| oracle-free | 5 | 2/2/2 | 6 | 4 of 6 |
| verifier-informed | 2 | 5/5/5 | 15 | 9 of 15 |
| verifier-informed | 3 | 3/3/3 | 9 | 6 of 9 |
| verifier-informed | 5 | 2/2/2 | 6 | 4 of 6 |
| verifier-informed | 10 | 1/1/1 | 3 | 2 of 3 |
