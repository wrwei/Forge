# Reference v3, SRanger run 4 (re-collection) — transcript audit

At the Amendment 7 pin `56cb43d7`; set up under Amendments 3, 4, 7 and 8. The run
collected before Amendment 7 is kept in `experiments/v3-superseded-a7/`.

Sources: `transcript.md` (Claude Code `/export`, 519 lines; 17 Bash and 10 Write
calls shown) and the session log (`scripts/session_audit_logs.sh`: 22 logged
commands, no Read call; tool listing Bash 22, Write 10; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 62 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook only (text hash `c0b542fbe3cb`); no Skill call |
| Setup | scaffold names `controller`; `.gitignore` and `pipeline.yaml` comments name no study; no root `README.md`; `docs/` holds only `corpus/` and `superpowers/specs/pipeline-manifest.schema.json` (the manifest schema Amendment 4 keeps) |
| Reads | all by shell: the SRanger inputs, the files `CLAUDE.md` names, layer prompts, Gradle scaffold and its own Java; one `ls docs/` in a setup check (folder names only). No pipeline source, other study, archive, staging folder, other worktree, git history, report or removed file; `pipeline.yaml` only through the preflight line; `.gitignore` not read. |
| How the session wrote | the nine Java files and `ITER_SUMMARY.md` with the Write tool; the trace by heredoc, checked by two inline Python snippets that read only the study's `requirement_all.json` and the session's own trace; the Layer 5 report by a scratchpad script (`post_codegen.py`) that opens no file for reading. No `cp`, `rsync`, `curl` or `wget` anywhere. |
| Self-check before the runner | a short scratchpad driver (`smoke/Smoke.java`) run against the session's own compiled classes |
| Writes | last write at export line 432, runner at line 451 |
| Runner | one call, iteration 1, in the foreground (`… run_claude_iteration.sh sranger 4 1 unavailable 2>&1 \| tail -60`) as the session's last command; the two runner lines in the command dump are one call logged twice. No iteration 2 was needed. |
| After the runner | nothing. Checked after the export: the worktree's Java (9 files) and trace are byte-identical to the staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` and `.gitignore` edits and the reports, besides setup's removals |

**Verdict: valid.**

**Result: converged at iteration 1** at `56cb43d7`. All twelve phases pass: FDR4
3 of 3 assertions; Dafny 1 file, 7 verified, 0 errors; Isabelle 1 theory, 21 lemmas
(28 s); vacuity passed with two advisory findings (a state with no enabled
operation when the environment offers nothing; the obstacle threshold 0.5 rounded
to 1 in the CSP model). The profile check passes with one rule-8 warning. No
`MAX_VALUE` in the Java. `Clock` (in `time/` in this run) converts
`System.nanoTime()` to seconds with the literal `1.0e9`; it appears in none of the
39 CSP files, the Isabelle theory, the `.rct`, the Dafny model or the RoboChart
model.

## Correction (1 November 2026, Amendment 9)

The Reads row above omits one read: the session ran
`cat forge.assets/corrections/csp_overrides.csp` (transcript lines 210–211) and
noted that the file's header names the LRE controller (lines 223–225). The file is at
the pin, names another study and lists LRE's safety constants; the audit's check for
forbidden reads did not cover `forge.assets/corrections/`. Under Amendment 9 this run
is superseded and re-collected; the record is kept in `experiments/v3-superseded-a9/`.
