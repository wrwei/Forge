# Cold v3, SRanger pass 2 (re-collection) — transcript audit

At the Amendment 7 pin `56cb43d7`. The pass collected before Amendment 7 is kept in
`experiments/v3-superseded-a7/`.

Sources: `transcript.md` (Claude Code `/export`, 655 lines; 14 Bash, 11 Write and
1 Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 36
logged commands, no Read call; tool listing Bash 36, Write 12, Edit 2,
ToolSearch 1, the Edit logged twice; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 107 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one message: the pasted prompt text, preceded by an IDE notice (counted by the export as a second user turn) |
| IDE notice | "The user opened the file …/forge-v3c7-run2-sranger/.gitignore in the IDE." The notice carries the path only. The session never opened `.gitignore` (no command, Read or grep on it); its one later mention, "gitignored", explains why its files do not appear in `git status`. See the deviation below. |
| Hooks and skills | the `superpowers` SessionStart hook only (text hash `c0b542fbe3cb`); no Skill call |
| Setup (Amendments 3, 4 and 7) | scaffold names `controller`; no root `README.md`; `docs/` holds only `corpus/` and the manifest schema |
| Reads | all by shell: the SRanger inputs, the files `CLAUDE.md` names and the layer prompts, the Gradle scaffold and its own Java. No pipeline source, other study, archive, staging folder, other worktree, git history, report or removed file. `pipeline.yaml` appears only in the preflight line and in two `git status --short -- … pipeline.yaml …` checks, which read nothing from it. |
| ToolSearch | one call, `select:Monitor` (a tool-definition lookup; opens no file) |
| Writes | Java under `java.generated.project/src/main/java/sranger/` (one self-review Edit adding `@RoboChartType("real")` to `Sensor.NO_READING_DISTANCE`, export line 412), the trace, the Layer 5 review and `ITER_SUMMARY.md`; last write at export line 521, runner at line 610 |
| Runner | one call (task `b9ng90ds7`); the two runner lines in the command dump are one call logged twice |
| After the runner | one read of the runner's background-task output. Checked after the export: the worktree's Java (9 files) and trace are byte-identical to the staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` edit and the reports, besides setup's removals |

**Verdict: valid**, with one non-voiding deviation.

**Deviation.** The VS Code window had `.gitignore` open, and Claude Code passed
its path to the session. `.gitignore` names two studies (the SRanger JUnit bundle
and the LRE reference-specification files) and is not scanned by the leakage
gate. The session received only the path and never read the file, so no study
content reached it; earlier IDE notices (`pipeline.yaml`, `README.md`) were
treated the same way. No session in the collection has read `.gitignore`.

**Result: converged** at `56cb43d7`. All twelve phases pass: FDR4 3 of 3
assertions; Dafny 1 file, 7 verified, 0 errors; Isabelle 1 theory, 21 lemmas
(27 s); vacuity passed with two advisory findings (a state with no enabled
operation when the environment offers nothing; the obstacle threshold 0.5 rounded
to 1 in the CSP model). The profile check passes with one rule-8 warning. No
`MAX_VALUE` or scientific-notation literal in the Java.
