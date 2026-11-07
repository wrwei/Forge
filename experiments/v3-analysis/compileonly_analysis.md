# Compile-only condition (Amendments 18 and 19)

| Study | Runs | Stop at iteration 1 | Verification-passing stop | Withheld failure at stop | Re-scored (A19) | Reference converged (at it. 1) | Cold passes all twelve |
|---|---|---|---|---|---|---|---|
| sranger | 5 | 4 | 5 |  |  | 5/5 (5) | 10/10 |
| lre | 5 | 3 | 5 |  | run-1 | 5/5 (5) | 9/10 |
| chemical_detector | 5 | 5 | 4 | run-2: fdr4 | run-5 | 5/5 (3) | 10/10 |
| total | 15 | 12 | 14 |  |  | 15/15 (13) | 29/30 |

Compile-only iteration 1 passing all twelve phases (equivalent to a cold pass): 11 of 15.
Pins: Compile-only ['1ad9301e', 'aaddf222']; Reference ['2db4c031', '5280e26e', '56cb43d7', 'd526ea5e'].
