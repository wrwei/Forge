# Requirement-derived Isabelle lemmas (Section 4.2)

Every lemma the pipeline generates in an Isabelle theory is structural (the store invariant),
internal consistency (deadlock freedom) or the placeholder requirement `R1 = True`. This
experiment states obligations derived from the requirements over the generated theories of two
reference controllers, outside the pipeline, and checks that Isabelle proves them and that a
wrong controller fails them.

Each `make_lemmas.py` appends to the generated theory one Hoare triple per mode, event the mode
listens for (or none), valuation of the conditions the requirements name in that mode, and
operation:

    H{st = mode ∧ offered = {e} ∧ valuation ∧ inv} op (nxt) {_. st ∈ admitted}

`admitted` is the set of targets of every requirement enabled in that case, or the current mode
when none is. The requirement conditions are bound to the theory's expressions by hand (`GUARD`
in each script). The theory itself is unchanged; every lemma is proved `by (zpog_full; simp)`.

- `sranger/`: from SR-Beh1–SR-Beh7 and SR-FR3, as compiled in
  `../requirements-conformance/multi-study/requirements_model_sranger.json`. 17 cases × 11
  operations = 187 lemmas.
- `lre/`: from the reference specification written from the requirements alone
  (`../reference-specification/`; rows and guards read from its `score.py`). 61 cases × 20
  operations = 1,220 mode-level lemmas, and 21 binding lemmas: the three Boolean flags the
  theory's guards read (`camActive`, `hcmActive`, `momReturn`) equal the specification's
  Collision, B_h ∨ B_0 ∨ B_v and ClearStatic conditions, established by the first operation and
  preserved by every operation.

## Results

| Theory | Lemmas | Proved | Failed |
|---|---|---|---|
| SRanger reference controller | 187 | 187 | none |
| SRanger, `MovingToTurning` goes to Final | 187 | 186 | `Req_Moving_obstacle_O_MovingToTurning` |
| LRE reference controller | 1,241 | 1,241 | none |
| LRE, `MOMToHCM` goes to OCM | 1,241 | 1,239 | the two mode-level lemmas where only HCM is admitted |
| LRE, `camActive` tests `tcpa > 0` instead of `tcpa ≥ 0` | 1,241 | 1,224 | the 17 binding lemmas of the operations that compute `camActive` |

Each control is an operation the extractor could generate for the changed Java. Build time is
about 40 s per SRanger theory and 2.5 minutes per LRE theory.

## Reproduce

    bash sranger/run_isabelle.sh
    bash lre/run_isabelle.sh

Isabelle runs in Docker through `scripts/isabelle-docker.sh`. `baseline/` is the reference
controller's generated theory; `reqlemmas/` and the control folders hold the theories with the
lemmas appended; `results/summary.txt` is the output of the runs above.

## Scope

Mode level only: the lemmas state which mode the controller goes to, not the events it sends.
SRanger's named constants are uninterpreted in its theory, so its thresholds are compared
symbolically; the LRE's are defined with their values. The binding holds from the first step on,
since the Isabelle `Init` leaves the data state free.
