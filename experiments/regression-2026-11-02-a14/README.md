# Regression: Amendment 14 (2 November 2026)

Pre-registration v3, Amendment 14 and its note (`review/prereg-v3-study-free.md` in the
paper repository). Pin before: `56cb43d7`. Pin after: `d714365c`.

Changes: Isabelle guard-function types taken from the model's `Sensors` declarations
(except the extractor's `nat` marker for unresolved names); Dafny brackets restored where
Java's precedence needs them; output-event values computed from fields and calls kept
(values reading a local variable are dropped as before); the macOS FDR4 memory monitor
reads the physical footprint; a killed FDR4 is reported as killed. The last two do not
touch extraction and were stub-tested.

Method, as for Amendments 2 and 6: each of 390 Java sources (every reported source, the
mutation campaign's 186 mutants, every collected iteration of the v3 collections and the
superseded ones, and the 14 staged iterations of Steam Boiler Reference run 2) is
extracted by the five extraction phases, each in its own JVM, once with the resources at
`56cb43d7` and once with those at `d714365c` (the compiled Java is the same; the Java
sources did not change). The RoboChart (`.rct`) and Dafny files are compared as sorted
non-comment lines, the Isabelle theories after canonicalisation.

| Result | Sources |
|---|---|
| identical | 372 |
| Isabelle type only | 14 |
| Isabelle type + send gains a value | 1 |
| Dafny brackets + Isabelle type | 2 |
| Dafny brackets + Isabelle type + send gains a value | 1 |
| outside the permitted kinds | 0 |

Every changed source is a Steam Boiler source, except Reference Chemical Detector run 4,
iterations 1 and 2 (two functions typed `Loc`, the enumeration the Java returns, instead
of `real`); its converged iteration 3 is unchanged. No mutant changes. Isabelle type
changes: `real` to `bool` 43, `real` to `int` 9, `real` to `Loc` 4.

A first run, before the note on Amendment 14, found 2 differences outside the permitted
kinds (two LRE trigger-swap mutants) and 18 type changes to the `nat` marker; both changes
were narrowed and the regression run again in full. This table is from the second run.

Files: `regression.tsv` (one row per source), `changed_lines.json` (every changed line).
