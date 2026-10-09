# Requirements-conformance check (RQ7, Section 4.9)

This directory holds the LRE requirements-conformance check reported in
Section 4.9 of the paper: the behavioural requirements compiled into a
transition relation, an exhaustive comparison with the extracted model,
and the scoring of the mutants that every verifier missed.

## Files

| file | contents |
|---|---|
| `requirements_model_lre.json` | the compiled requirements (19 transitions, 11 atoms) |
| `prose_formula_table.md` | each requirement's text next to its formula |
| `ambiguity_log.md` | the 8 interpretive decisions, each resolved from the text alone |
| `ordering_attestation.txt` | SHA-256 hashes of the three files above, recorded before any implementation artefact was read |
| `archived_conformance_diff.csv` | verdict for every (mode, event, guard valuation) cell, 458,752 rows |
| `archived_survivor_scoring.csv` | one row per LRE mutant surviving every verifier, 62 rows |
| `reqconf_report.md` | the analysis report |
| `run_reqconf.py` | the program that produces both CSVs |

## Reproducing the results

From the repository root, with Python 3, numpy and pandas:

```
python3 experiments/requirements-conformance/run_reqconf.py --check
```

The program first checks the SHA-256 of `requirements_model_lre.json`
against `ordering_attestation.txt` and stops if they differ, so the
comparison always runs on the hashed model. It then reads the extracted
LRE model from `reference-runs/lre/formal-artefacts/isabelle/`, and the
mutation-testing results and mutant models from `experiments/mutation-testing/`.
With `--check` it compares both outputs byte for byte with the CSVs in
this directory. A run takes a few seconds and should end with:

```
wrote archived_conformance_diff.csv: 458752 rows
wrote archived_survivor_scoring.csv: 62 rows, 29 scoreable, 15 killed
both outputs are byte-identical to the shipped copies
```

Use `--out DIR` to choose where the outputs are written; the default is a
new temporary directory.
