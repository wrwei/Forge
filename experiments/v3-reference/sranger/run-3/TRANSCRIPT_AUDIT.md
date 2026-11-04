# Reference v3, SRanger run 3 (re-collection) — transcript audit

At the Amendment 7 pin `56cb43d7`; set up under Amendments 3, 4, 7 and 8. The run
collected before Amendment 7 is kept in `experiments/v3-superseded-a7/`.

Sources: `transcript.md` (Claude Code `/export`, 458 lines; 14 Bash and 10 Write
calls shown) and the session log (`scripts/session_audit_logs.sh`: 22 logged
commands, 2 Read calls; tool listing Bash 22, Write 11, Read 2, the Read logged
twice; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 69 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook only (text hash `c0b542fbe3cb`); no Skill call |
| Setup | scaffold names `controller`; `.gitignore` and `pipeline.yaml` comments name no study; no root `README.md`; `docs/` holds only `corpus/` and the manifest schema |
| Reads | the SRanger requirements (Read tool, logged twice), the other SRanger inputs, the files `CLAUDE.md` names, layer prompts and Gradle scaffold (by shell), its own Java. One command (log line 29) also tried `cat java.generated.project/README.md`; that file does not exist at the pin, so nothing was read. No pipeline source, other study, archive, staging folder, other worktree, git history, report or removed file; `pipeline.yaml` only through the preflight line (export line 167); `.gitignore` not read. |
| Writes | Java under `java.generated.project/src/main/java/sranger/`, the trace, the Layer 5 review and `ITER_SUMMARY.md`; last write at export line 398, runner at line 419 |
| Runner | one call, iteration 1, run in the foreground with its output redirected to a scratchpad log, followed in the same command by `tail -40` of that log; the two runner lines in the command dump are one call logged twice. It is the session's last command. No iteration 2 was needed. |
| After the runner | nothing beyond the `tail` in the runner's own command. Checked after the export: the worktree's Java (9 files) and trace are byte-identical to the staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` and `.gitignore` edits and the reports, besides setup's removals |

**Verdict: valid.**

**Result: converged at iteration 1** at `56cb43d7`. All twelve phases pass: FDR4
3 of 3 assertions; Dafny 1 file, 7 verified, 0 errors; Isabelle 1 theory, 21 lemmas
(28 s); vacuity passed with two advisory findings (a state with no enabled
operation when the environment offers nothing; the obstacle threshold 0.5 rounded
to 1 in the CSP model). The profile check passes with one rule-8 warning. No
`MAX_VALUE` or scientific-notation literal in the Java.
