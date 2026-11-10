# Guard-level enumeration (Section 5.5)

For every mode, truth value of each named guard predicate and input event, the enumeration
compares the next mode the Java controller's `step()` takes with the set of target modes the
extracted Isabelle theory admits. A state agrees when the theory admits exactly the Java's next
mode, or only the idle operation when no Java branch applies. A guard atom the harness does not
recognise is an error, not a pass.

| Controller | States | Agree |
|---|--:|--:|
| LRE | 7,168 | 7,168 |
| SRanger | 60 | 60 |
| Chemical Detector, gas analysis | 100 | 100 |
| Chemical Detector, movement | 160 | 160 |
| Total | 7,488 | 7,488 |

In every state exactly one operation is enabled. The Java is that of the reference controllers in
`reference-runs/<study>/java`; the LRE's next mode is read from its source, the other three from
transcriptions of their `step()` bodies (`sr_java_next`, `cd_java_next`, `mv_java_next` in
`enumeration_lib.py`).

## Reproducing

From the repository root, with JDK 21, a built extractor (`cd forge.transformations && ./gradlew classes`)
and Python 3 (standard library only):

```
bash scripts/generate_isabelle.sh /tmp/enum-thy
python experiments/guard-level-enumeration/run_enumeration.py --thy-root /tmp/enum-thy
```

## Files

- `run_enumeration.py`, `enumeration_lib.py`, `enumeration_studies.py`: the enumeration
- `enumeration_rows.csv`: every state (7,488 rows): study, mode, event, the guard values, the
  Java's next mode, the modes and operations the theory admits, and whether they agree
