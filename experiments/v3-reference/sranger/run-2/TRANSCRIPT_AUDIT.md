# Reference v3, SRanger run 2 (re-collection) — transcript audit

At the Amendment 7 pin `56cb43d7`; set up under Amendments 3, 4, 7 and 8. The run
collected before Amendment 7 is kept in `experiments/v3-superseded-a7/`.

Sources: `transcript.md` (Claude Code `/export`, 660 lines; 13 Bash and 14 Write
calls shown) and the session log (`scripts/session_audit_logs.sh`: 23 logged
commands, 10 Read calls; tool listing Bash 23, Write 14, Read 10, Reads logged
twice; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 80 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook (text hash `c0b542fbe3cb`); no Skill call. Four further injected records: the VS Code Java extension's classpath diagnostic for four of the Java files, each naming the neutral project `controller-java.generated.project` |
| Setup | scaffold names `controller`; `.gitignore` and `pipeline.yaml` comments name no study; no root `README.md`; `docs/` holds only `corpus/` and the manifest schema |
| Reads | the SRanger inputs and the five files `CLAUDE.md` names under `forge.assets/prompts/` (Read tool), the layer prompts and Gradle scaffold (by shell), its own Java. `pipeline.yaml` through the preflight line and `sed -n 1,40p pipeline.yaml` in a setup check (permitted under Amendment 5; those 40 lines name no study). No pipeline source, other study, archive, staging folder, other worktree, git history, report or removed file; `.gitignore` not read. |
| Writes | Java under `java.generated.project/src/main/java/sranger/`, the trace, the Layer 5 review and `ITER_SUMMARY.md`; last write at export line 573, runner at line 587 |
| Runner | one call, iteration 1, run in the foreground (`… run_claude_iteration.sh sranger 2 1 unavailable 2>&1 \| tail -60`) as the session's last command; the two runner lines in the command dump are one call logged twice. No iteration 2 was needed. |
| After the runner | nothing: the session's last action is its report. Checked after the export: the worktree's Java (9 files) and trace are byte-identical to the staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` and `.gitignore` edits and the reports, besides setup's removals |

**Verdict: valid.**

**Result: converged at iteration 1** at `56cb43d7`. All twelve phases pass: FDR4
3 of 3 assertions; Dafny 1 file, 7 verified, 0 errors; Isabelle 1 theory, 21 lemmas
(28 s); vacuity passed with two advisory findings (a state with no enabled
operation when the environment offers nothing; the obstacle threshold 0.5 rounded
to 1 in the CSP model). The profile check passes with one rule-8 warning. No
`MAX_VALUE` or scientific-notation literal in the Java.
