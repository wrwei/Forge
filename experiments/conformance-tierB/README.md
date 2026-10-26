# Guard-level conformance enumeration (Figure 2, Section 5.5)

For every mode, guard valuation and input event, the enumeration compares the
next mode the Java controller takes with the set of targets the extracted
Isabelle model admits. A state agrees when the admitted target set is exactly
the Java's next mode (the current mode when nothing is enabled).

| study | states | archived extraction | published extractor |
|---|--:|--:|--:|
| LRE | 7,168 | 88.8% | 100% |
| SRanger | 60 | 80.0% | 100% |
| Chemical Detector | 100 | 92.0% | 100% |

## Reproducing

From the repository root, with Python 3 (standard library only):

```
# archived extraction: the theories in reference-runs/
python experiments/conformance-tierB/run_tierb.py prefix

# Current extractor: regenerate the theories from the same Java, then enumerate
bash scripts/generate_isabelle.sh /tmp/tierb-thy
python experiments/conformance-tierB/run_tierb.py fixed --thy-root /tmp/tierb-thy
```

`generate_isabelle.sh` needs JDK 21 and a built extractor
(`cd forge.transformations && ./gradlew classes`).

The two settings differ only in how the triggering event is read from a theory.
The archived extraction puts no event in an operation's precondition, so the
event is read from the operation's trace update; the current extractor puts it
in the precondition as `e ∈ offered`. Both settings use the same Java side and the
same agreement definition. For the LRE the Java side is read from the extracted
`java_rel` table; for SRanger and the Chemical Detector it is a hand-written
transcription of the controllers' `step()` bodies (`sr_java_next`, `cd_java_next`
in `tierb_harness_gen.py`), checked against the reference-run sources. A guard atom
the harness does not recognise is an error, not a pass.

## Files

- `run_tierb.py`, `tierb_harness_gen.py`, `tierb_studies.py`: the enumeration
- `tierb_prefix_rows.csv`, `tierb_fixed_rows.csv`: every state, both arms (7,328 rows each)
- `tierB_lre_conformance.csv`, `tierB_lre_disagreements.csv`, `tierB_lre_agreement.png`:
  the LRE enumeration as first reported (its rows agree with `tierb_prefix_rows.csv`)
