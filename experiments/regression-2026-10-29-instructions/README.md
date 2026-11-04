# Regression check: study-free instructions and feedback text (29 October 2026)

The commit that introduces the study-free instructions (pre-registration v3) changes
three pipeline files besides the instructions: the profile checker's fix directives
(`StructuralLinter.java`), one vacuity-audit message (`vacuity.py`), and the comment the
Isabelle generator writes above the deadlock-freedom lemma of a machine with a terminal
state (`thy_generation_rule.egl`). None of these may change a verdict.

## Method

- `StructuralLinter.java` and `vacuity.py`: the old and new files were compared with
  comments removed and every string literal replaced by one placeholder (adjacent
  literals merged). Both are identical under that comparison, so only message text
  changed.
- `thy_generation_rule.egl`: every Java source behind a reported result (the 339 of the
  28 October regression, plus the 15 cold-baseline-v2 passes) was run through
  T2M → M2M → Dafny/RCT/Isabelle generation twice, with the old and the new template,
  and the generated `*_Beh.thy` files were compared with Isabelle comments removed.
- The dashboard test suite passes identically on the published tree and on this one
  (86 passed; two tests needing `fastapi` or a local socket excluded on both).

## Result

All 354 sources generate with both templates (`regression.tsv`: source, generation OK
with the old template, with the new, theory files, comparison without comments, full-text
comparison). Without comments, all 354 are identical. 33 differ in a comment only: the
machines with a terminal state, whose comment no longer says the obligation is false and
no longer asks for a tick-triggered self-loop (the `until` clause makes the obligation
provable).
