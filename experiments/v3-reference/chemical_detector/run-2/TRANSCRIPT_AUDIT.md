# Reference v3, Chemical Detector run 2 — transcript audit

At the Amendment 2 pin `2db4c031`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 1469 lines; 17 Bash, 26 Write and
13 Update calls shown) and the session log (`scripts/session_commands.py`: 38
logged commands, 1 Read call; a tool listing of the log: Bash 38, Write 33, Edit
26, Read 1; every Edit appears twice, as with the runner lines, so 13 distinct
edits; `scripts/session_hooks_skills.py`).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 132 responses |
| Claude Code | 2.1.312 throughout |
| Operator input | one message: the pasted prompt text. The export shows a second `❯` entry before it, the VS Code extension's notice that `LICENSE` was open in the editor. **Deviation (not voiding):** the session never opened `LICENSE` (no command or Read names it); the file names no study. |
| Hooks and skills | the `superpowers` SessionStart hook only; no Skill call |
| Setup (Amendments 3 and 4) | no root `README.md`; `docs/` holds only `corpus/` and the manifest schema; no `.claude/memory`, `commands` or `skills`; no `scripts/envmodel_b` |
| Reads before iteration 1 | the Chemical Detector inputs (requirements by Read tool); the files `CLAUDE.md` names and the layer prompts (by shell); the Gradle files; its own Java. No pipeline source, other study, archive, staging folder, git history or removed file. |
| Iteration 1 | Java written and self-reviewed (Writes from export line 205, Updates to line 1086; a `javac` build and a smoke test in the session's scratchpad); runner call at line 1155 |
| Between the iterations | the session read one report, `forge.assets/corrections/post_preflight.md` (twice), which a Reference run may read; no other report and no pipeline file. It changed intensity from `double` to `int` (`nat`) in four Java files (`ChemicalDetectorConstants`, `GasSensor`, `GasAnalysisController`, `ChemicalSensor`; Updates at export lines 1198–1275), adjusted its scratchpad smoke test, regenerated its trace and Layer 5 review, and recorded in the review that the missed annotation "is probably a lint defect". |
| Iteration 2 | runner call after the last Java edit; no command after it |
| Runner | two iterations (`iter-1`, `iter-2`); the command dump's three runner lines are iteration 1 logged twice and iteration 2 once |
| Java | the worktree's Java byte-identical to `iter-2` |
| Working tree | unrestricted `git status`: setup's `pipeline.yaml` edit, Amendment 3's removals and the reports in `forge.assets/corrections/` |

**Verdict: valid.**

**Result: converged at iteration 2** at `2db4c031` (condition `V3-reference-loop`).

- **Iteration 1:** eleven phases pass, including FDR4 9 of 9 assertions, Dafny 2
  files 22 verified, Isabelle 2 theories 76 lemmas. The profile check fails on one
  rule-4 error, "field `GasSensor.i` is a double without `@RoboChartType("real")`",
  although the record component carries the annotation. This is a profile-checker
  false positive: the checker inspects the implicit field of each record
  component, and the Java parser (Spoon) copies the component's annotation onto
  that field only when the record is not in the source root's top-level package
  (reproduced with a one-line record in `p` and in `p.domain`; see the collection
  README). `GasSensor` is in the top-level `chemical_detector` package. Two rule-8
  warnings.
- **Iteration 2:** all twelve phases pass: FDR4 9 of 9; Dafny 2 files, 22
  verified, 0 errors; Isabelle 2 theories, 76 lemmas (3 min 46 s); vacuity passed
  with one advisory per controller. Two rule-8 warnings.

Decision (operator, 30 October): the pin is kept, so the second iteration is
counted, and the iteration-1 failure is reported as a profile-checker false
positive.
