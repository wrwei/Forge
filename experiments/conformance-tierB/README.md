# Guard-level conformance enumeration (Figure 2, Section 5.5)

For every mode, guard valuation and input event, the enumeration compares the
next mode the Java controller takes with the set of targets the extracted
Isabelle model admits. A state agrees when the admitted target set is exactly
the Java's next mode (the current mode when nothing is enabled).

| study | states | Table 4 extractor | current extractor |
|---|--:|--:|--:|
| LRE | 7,168 | 88.8% | 100% |
| SRanger | 60 | 80.0% | 100% |
| Chemical Detector | 100 | 92.0% | 100% |

## Reproducing

From the repository root, with Python 3 (standard library only):

```
# Table 4 extractor: the archived theories in reference-runs/
python experiments/conformance-tierB/run_tierb.py prefix

# Current extractor: regenerate the theories from the same Java, then enumerate
bash scripts/generate_isabelle.sh /tmp/tierb-thy
python experiments/conformance-tierB/run_tierb.py fixed --thy-root /tmp/tierb-thy
```

`generate_isabelle.sh` needs JDK 21 and a built extractor
(`cd forge.transformations && ./gradlew classes`).

The two arms differ only in how the triggering event is read from a theory.
The Table 4 extractor puts no event in an operation's precondition, so the
event is read from the operation's trace update; the current extractor puts it
in the precondition as `e ∈ offered`. Both arms use the same Java side and the
same agreement definition.

## Files

- `run_tierb.py`, `tierb_harness_gen.py`, `tierb_studies.py`: the enumeration
- `tierb_prefix_rows.csv`, `tierb_fixed_rows.csv`: every state, both arms (7,328 rows each)
- `tierB_lre_conformance.csv`, `tierB_lre_disagreements.csv`, `tierB_lre_agreement.png`:
  the LRE enumeration as first reported (its rows agree with `tierb_prefix_rows.csv`)
