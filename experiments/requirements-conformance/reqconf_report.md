# Requirements-conformance check — LRE (Section 4.9, RQ7)

## 1. Ordering attestation

The requirements model, the text-to-formula table and the ambiguity log were
compiled before any implementation artefact was read; `ordering_attestation.txt`
records their SHA-256 digests and states that order. Both programs in this folder
check `requirements_model_lre.json` against the recorded digest before they run.
The attestation pins the files and states the ordering; it does not establish
independence of construction, since the relation was compiled by the authors.

```
sha256(requirements_model_lre.json) = d2491f4e6ce75f4b2b0bfafae99664b589948fa5abf46a2a3d5bce94db8d7a4b
sha256(prose_formula_table.md)      = 61c635f75c45a69d24bde02d41ffbf0e794d6ea9ce0038cb740df29f4429d932
sha256(ambiguity_log.md)            = 9575486033cb28eac63b556cddd66cdf8ad148e569902df1563d84f353b566b6
```

## 2. Scope and translation

Compiled from LRE-Beh1..Beh19 (the behavioural requirements) and the variable
declarations LRE-Var1..Var8. The LRE-GP entries are excluded: they contain
literal Java guard code, and including them would reintroduce the derivation
from the implementation. LRE-DM4 (constant values) is also outside scope, so
named thresholds stay symbolic in the requirements model and are bound to the
implementation's declared constants only at comparison time. 18 transitions and
one closure constraint (Beh19) compiled; 11 atomic guard predicates; 8
ambiguities logged, all resolved from the text alone; no conjunct left
underdetermined.

## 3. Conformance of the extracted model (`conformance_diff.csv`)

Extracted side: the Isabelle theory the extractor produces from the unmutated
LRE (`../mutation-testing/outputs/baseline-lre/isabelle/LreController_Beh.thy`).
The extracted machine does not carry the controller's three derived flags
(`camActive`, `hcmActive`, `momReturn`) as computed variables, so the
enumeration treats them as free inputs: 2^14 guard valuations x 4 modes x 7
events = 458,752 cells.

| space | cells | disagreements |
|---|---:|---:|
| flags consistent with their defining predicates (the states the controller can occupy) | 57,344 | **0** |
| full space | 458,752 | 115,520, all in cells where a flag contradicts its defining predicate (`mechanism = frozen_data_plane`) |

On the consistent space the verdicts are 24,320 exact agreements, 20,576 cells
where both sides stay, and 12,448 refinements (several requirements transitions
enabled, no priority stated, the model takes one of them). Produced and checked by
`run_reqconf_conformance.py --check`.

## 4. Survivor scoring (`survivor_scoring.csv`)

Pool: the 54 LRE mutants of `../mutation-testing/kill_table.csv` that survive every
verifier. For each, the mutant's theory is evaluated over the hybrid grid and
compared with the requirements relation; a kill is at least one cell with a
disagreement or an output violation that the unmutated theory does not have.
Produced and checked by `run_reqconf.py --check`.

| class | LRE survivors | scoreable | killed | kill rate (scoreable) |
|---|---|---|---|---|
| relop_flip | 10 | 10 | 3 | 30% |
| cmp_swap | 3 | 3 | 3 | 100% |
| trigger_swap | 7 | 7 | 7 | 100% |
| off_by_one | 10 | 6 | 3 | 50% |
| branch_swap | 14 | 14 | 0 | 0% |
| drop_negation | 7 | 7 | 7 | 100% |
| sentinel_init | 3 | 0 | 0 | — |
| **total** | **54** | **47** | **23** | **48.9%** |

23 of 54 (42.6%) of all LRE survivors; 23 of 47 (48.9%) of the scoreable ones.

**Scoreable survivors the relation misses (24):** the 14 branch-priority swaps
(the requirements state no priority between the swapped branches, so the relation
admits either order), 7 relational-operator flips and 3 off-by-one changes inside
the computation of a guard value, which the theory carries only as the resulting
guard; the relation's abstraction cannot distinguish them from the unmutated
controller. The independently written specification of `../blind-spec/` reads the
Java and kills 9 of these 10.

**Not scoreable (7):** the 3 sentinel initialisers and 4 off-by-one changes whose
theory is identical to the unmutated one (the defect changes only the Dafny
program or the RoboChart model, which this check does not read).

## 5. Limitations

- The relation is the authors' translation of the requirements; the attestation
  fixes what was compiled and when, not who could have compiled it differently.
  The specification of `../blind-spec/` is the independently authored reference.
- The check compares mode-level transitions and transition-local outputs; a defect
  inside a guard's computation reaches it only when it changes the resulting guard.
- Thresholds are bound to the implementation's declared constants, so a mutant of
  a constant's value is scored against its own value.
