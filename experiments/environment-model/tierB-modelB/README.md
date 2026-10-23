# Guard-level conformance under environment model B

The enumeration of `experiments/conformance-tierB` re-run on theories the
repaired generator (environment model B) extracts from the same Java
(`reference-runs/<study>/java`), against the same Java step function.

| study | cells | model A (shipped `tierb_fixed_rows.csv`, reproduced today) | model B |
|---|--:|--:|--:|
| LRE | 7,168 | 100% | 100% |
| SRanger | 60 | 100% | 100% |
| Chemical Detector | 100 | 100% | 100% |

The agreement figure is unchanged; what changes is what the model says in
the 2,412 cells (of 7,328) where no Java branch applies. Under model A no
operation was enabled there and the harness scored the cell by its "nothing
enabled = stay" convention. Under model B the `Idle_X` operation is enabled in
exactly those 2,412 cells and nowhere else, and in every one of the 7,328
cells exactly one operation is enabled: the extracted step relation is total
and deterministic, as the Java step function is. The two row files differ
only in the `model_ops`/`n_adm` columns of those 2,412 rows.

The harness (`tierb_harness_gen.py`) gained one capability for this run:
`Idle_X` preconditions are conjunctions of negated conjunctions, so top-level
conjuncts are now split with parenthesis depth and a negated conjunction is
evaluated recursively with the offer in scope. The archived `prefix` and
model-A `fixed` rows are reproduced byte-identically by the extended harness.
`theories/<study>/` are the model-B theories enumerated here.
