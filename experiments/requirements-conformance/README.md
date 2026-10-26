# Requirements-conformance check (RQ7, Section 4.9)

This directory holds the LRE requirements-conformance check reported in
Section 4.9 of the paper: the behavioural requirements compiled into a
transition relation, an exhaustive comparison with the extracted model,
and the scoring of the mutants that every verifier of the mutation-testing
experiment (`../mutation-testing`) accepts.

## Files

| file | contents |
|---|---|
| `requirements_model_lre.json` | the compiled requirements (19 transitions, 11 atoms) |
| `prose_formula_table.md` | each requirement's text next to its formula |
| `ambiguity_log.md` | the 8 interpretive decisions, each resolved from the text alone |
| `ordering_attestation.txt` | SHA-256 hashes of the three files above, with the written record that they were compiled before any implementation artefact was read |
| `conformance_diff.csv` | verdict for every (mode, event, guard valuation) cell of the extracted LRE model, 458,752 rows |
| `survivor_scoring.csv` | one row per LRE mutant surviving every verifier, 54 rows (47 scoreable, 23 killed) |
| `idle_check.csv` | per theory, the check that each `Idle_<State>` operation is the complement of that state's transitions |
| `reqconf_report.md` | the analysis report |
| `reqconf_relation.py` | the relation, the grid and the comparison (library used by the two programs below) |
| `run_reqconf_conformance.py` | produces `conformance_diff.csv` |
| `run_reqconf.py` | produces `survivor_scoring.csv` and `idle_check.csv` |
| [`multi-study/`](multi-study/) | the same comparison for SRanger and both Chemical Detector controllers, with survivor scoring (31 of 80 killed) |

## Reproducing the results

From the repository root, with Python 3, numpy and pandas:

```
python3 experiments/requirements-conformance/run_reqconf_conformance.py --check
python3 experiments/requirements-conformance/run_reqconf.py --check
```

Both programs first check the SHA-256 of `requirements_model_lre.json`
against `ordering_attestation.txt` and stop if they differ, so the comparison
always runs on the hashed model. The first reads the extracted model of the
unmutated LRE (`../mutation-testing/outputs/baseline-lre/isabelle/`) and
compares it with the relation over the full abstract space; with `--check` it
confirms the result is byte-identical to the shipped `conformance_diff.csv`.
Expected: 458,752 rows; 0 disagreements on the 57,344 cells where the derived
flags are consistent with their defining predicates (100%); 115,520 full-space
disagreements, all in the guard values the extraction leaves uncomputed.

The second reads `../mutation-testing/kill_table.csv`, scores every LRE mutant
with `disposition == survived` against the relation, and with `--check`
compares the verdicts row by row with the shipped `survivor_scoring.csv`.
Expected: 54 rows, 47 scoreable, 23 killed. A mutant whose theory is identical to
the unmutated one (the defect changes only the Dafny program or the RoboChart
model) is recorded as not scoreable.

The independently written reference specification of Section 4.9.1 and its
scoring of the same 54 mutants are in [`../blind-spec/`](../blind-spec/).
