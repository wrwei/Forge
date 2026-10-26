# Compile-only ablation stop points, replayed with the published extractor

One pipeline pass per recorded stop-point Java (the last iteration of each archived run).
`ships broken` = at least one of fdr4 / dafny_verify / isabelle_verify / vacuity failed at the stop point.

| Study | Run | Verdict | fdr4 | dafny_verify | isabelle_verify | vacuity | Ships broken |
|---|---:|---|---|---|---|---|---|
| sranger | 1 | yes | passed | passed | passed | passed | no |
| sranger | 2 | yes | passed | passed | passed | passed | no |
| sranger | 3 | yes | passed | passed | passed | passed | no |
| sranger | 4 | yes | passed | passed | passed | passed | no |
| sranger | 5 | yes | passed | passed | passed | passed | no |
| lre | 1 | yes | passed | passed | passed | passed | no |
| lre | 2 | yes | passed | passed | passed | passed | no |
| lre | 3 | yes | passed | passed | passed | passed | no |
| lre | 4 | yes | passed | passed | passed | passed | no |
| lre | 5 | yes | passed | passed | passed | passed | no |
| chemical_detector | 1 | yes | passed | passed | passed | passed | no |
| chemical_detector | 2 | yes | passed | passed | passed | passed | no |
| chemical_detector | 3 | yes | passed | passed | passed | passed | no |
| chemical_detector | 4 | yes | failed | passed | passed | passed | yes |
| chemical_detector | 5 | yes | failed | passed | passed | passed | yes |

| Study | Ships broken |
|---|---:|
| sranger | 0 of 5 |
| lre | 0 of 5 |
| chemical_detector | 2 of 5 |
| **Total** | **2 of 15** |
