# Requirements-level scoring of the survivors (Section 4.9)

The 134 mutants that survive every verifier (`../kill_table.csv`, `disposition == survived`:
54 LRE, 21 SRanger, 59 Chemical Detector) scored with the three requirements-level checks:

| instrument | reads | program and output |
|---|---|---|
| compiled LRE requirements relation | the mutant's `LreController_Beh.thy` | `../../requirements-conformance/run_reqconf.py` → `survivor_scoring.csv` (54 rows, 47 scoreable, 23 killed) |
| compiled SRanger and Chemical Detector relations | `SRangerController_Beh.thy`, or `GasAnalysisController_Beh.thy` + `MovementController_Beh.thy` | `../../requirements-conformance/multi-study/score_survivors.py` → `multi_survivor_scoring.csv` (80 rows, 31 killed) |
| independently written LRE reference specification | the mutant's Java | `../../blind-spec/blindspec3_score.py` → `blindspec3_survivor_scoring.csv` (54 rows, 32 killed) |

`combined_verdicts.csv` joins the three: one row per survivor with each instrument's scoreable and
killed flags, `killed_by_any` and `remaining`. Totals: 63 of 134 killed by at least one check
(LRE 32 of 54, SRanger 8 of 21, Chemical Detector 23 of 59); of the 71 remaining, 49 are
branch-priority swaps the requirements admit (they state no priority between the swapped branches)
and 22 change no decision at the level the relations describe (each row's `mechanism` says which).
The specification's 32 kills contain the compiled relation's 23.

`summarize.py` recomputes the totals from the three CSVs; `run_all.sh` runs the three programs
with `--check` and then `summarize.py`.
