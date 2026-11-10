# Mutation testing with behaviour-preserving edits (Section 4.8.2)

The third of the paper's three mutation-testing campaigns:

1. [`../mutation-testing/`](../mutation-testing/): mutation testing of the generated Java (Section 4.8, Table 9).
2. `../property-mutation-*/` (summary in [`../property-mutation-summary/`](../property-mutation-summary/)): mutation testing of the obligations (Section 4.8.1, Table 10).
3. This folder: mutation testing with behaviour-preserving edits (Section 4.8.2).

## What is here

| File | Content |
|---|---|
| `make_mutants.py` | builds the 35 edits to the reference Java (`../../reference-runs/<study>/java`) into `mutants/` |
| `mutants_manifest.csv` | each edit: study, class, file, line, before and after |
| `behaviour_check.py`, `behaviour_check.csv` | runs each edited controller and its original through the harnesses of [`../differential-guard-check/`](../differential-guard-check/) on the same inputs; the next mode agrees at every step for all 35 |
| `artefacts/<id>/` | the generated RoboChart model, Dafny program, CSP and Isabelle theory of each edit and of the three baselines, produced with the campaign driver of `../mutation-testing/driver/` |
| `results_regen_dafny.csv`, `regen_artefacts.log` | which artefacts changed, and Dafny's verdict |
| `results_fdr4.csv`, `fdr4/` | FDR4 verdicts and logs |
| `results_isabelle.csv`, `isabelle/` | Isabelle verdicts and build logs |
| `stated_values.csv` | the comparison with the values the requirements state (`../requirements-constants-check/`) |
| `scope.csv` | which edits are reported |

## Scope and result

As in Section 4.8, only edits inside the abstraction boundary that change a generated artefact are
reported: 21 of the 35 (LRE 6, SRanger 6, Chemical Detector 9). Of the other 14, 12 change no
artefact and 2 move a guard into a helper method, which the model represents as an uninterpreted
value, outside the boundary.

On the 21: Dafny verifies all 21; FDR4 passes every assertion on the 20 whose CSP changed; Isabelle
verifies every changed theory. The stated-values comparison reports the two renamed constants as
not found and nothing else.

`chemical_detector_bp_reorder_03` exchanges `vehicle.flag()` and `vehicle.move(0.0, Angle.Front)`
in one entry action: every mode decision is unchanged, and the two outputs of that step are
emitted in the other order.

## Re-running

    python3 make_mutants.py
    python3 behaviour_check.py                          # needs Java 21
    # artefacts and Dafny, then FDR4 and Isabelle, with ../mutation-testing/scripts/
    # (regen_campaign.py, campaign_verifiers.sh) pointed at this folder's mutants/
