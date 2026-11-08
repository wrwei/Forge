# Changed artefact lines traced to their injection sites

`trace_changed_lines.py` links every changed line of every generated artefact of each mutant
inside the abstraction boundary back to the Java lines the mutant changed, through the
pipeline's per-phase trace records (`trace_m2m.json`: RoboChart transition to Java lines;
`trace_isabelle.json`: Z operation to theory line; `trace_dafny.json`: Dafny element to program
lines). The rules are in the script's docstring. Output: `changed_lines_trace.csv`, one row per
mutant and artefact, with the changed, traced and untraced line counts and a sample of the
untraced lines.

## Result (campaign at `8d64b181`)

- 140 of the 152 inside-boundary mutants change at least one artefact line in content. The other
  12 are the swaps of two whole mode blocks (`if (currentMode == A) ... else if (currentMode ==
  B)`): their theory, Dafny program and RoboChart model differ from the original only in the
  numbering of transitions and the order of the machine's operation list, which the campaign's
  normalisation (`canon.py`: set elements and disjuncts) does not cover. They are equivalent
  mutants and were verified as such.
- Of the 1,420 changed lines (947 theory, 318 Dafny, 155 RoboChart), 1,401 trace to the element
  generated from the injection site or mention an identifier of the mutated lines: all 318 Dafny
  lines, 946 of 947 theory lines, 137 of 155 RoboChart lines. 131 of the 140 mutants have every
  changed line traced.
- The 19 untraced lines (9 mutants, all Chemical Detector) are declaration lines that follow from
  a traced change in the same artefact: the state machine's `uses ... requires ...` interface
  list and one event declaration, where a trigger swap leaves the branch's payload variable
  undeclared and the extractor adds an interface for it (8 trigger swaps), and the machine's
  operation list where a dropped negation adds an operation (1).

## Two things the analysis found about the trace records

1. `trace_m2m.json` gives the wrong `java_file` (and `java_qualified_name`) for every transition
   of the Chemical Detector: the file of `ChemDetectorApp.step` instead of the controller whose
   `step()` contains the branch. `TransformPhase.enrichWithJavaSource` looks the Java element up
   by its simple name (`step`) and takes the first class with a method of that name. The line
   numbers (`java_line_start/end`, written by the ETL) are right, and `java_element` names the
   right class, so the script matches on `java_element`. No verifier input is affected.
2. `trace_dafny.json` gives each transition method the span of its header and contract only, not
   its body; the script assigns a line to the last element that starts before it.
