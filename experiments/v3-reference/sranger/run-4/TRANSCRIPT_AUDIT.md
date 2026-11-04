# Reference v3, SRanger run 4 (re-collected under Amendment 9) — transcript audit

At the Amendment 7 pin `56cb43d7`; set up under Amendments 3, 4, 7, 8 and 9. The
run it replaces is kept in `experiments/v3-superseded-a9/v3-reference/sranger/run-4/`.

Sources: `transcript.md` (Claude Code `/export`, 716 lines; 13 Bash and 14 Write
calls shown) and the session log (`scripts/session_audit_logs.sh`: 36 logged
commands, no Read call; tool listing Bash 36, Write 14; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 87 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook (text hash `c0b542fbe3cb`); no Skill call. Four further injected records: the VS Code Java extension's classpath diagnostic for four of the Java files, each naming the neutral project `controller-java.generated.project` |
| Setup | `setup_ok.json`: complete, all checks passed (the gate among them); `forge.assets/corrections/csp_overrides.csp` absent (`git status` shows it deleted by setup); scaffold names `controller`; `.gitignore` and `pipeline.yaml` comments name no study; no root `README.md`; `docs/` holds only `corpus/` and the manifest schema Amendment 4 keeps |
| Reads | all by shell: the SRanger inputs, the files `CLAUDE.md` names, layer prompts, Gradle scaffold and its own Java. One listing of `forge.assets/corrections/` before any write (command-log line 45), when the folder was empty. Inline Python read only the study's `requirement_all.json` and the session's own Java and `post_codegen.json`. No pipeline source, other study, archive, staging folder, other worktree, git history, report or removed file; `pipeline.yaml` only through the preflight line; `.gitignore` not read. |
| How the session wrote | the nine Java files and `ITER_SUMMARY.md` with the Write tool; the trace and the Layer 5 report by inline Python with the content inline. No `cp`, `rsync`, `curl` or `wget` anywhere. |
| Self-check before the runner | a short scratchpad driver (`smoke/Smoke.java`, written by heredoc, rewritten once) run against the session's own compiled classes |
| Writes | last Write at transcript line 650; the runner command (its Bash call, `cd` into the worktree followed by the `export FORGE_*` lines) at transcript line 669, run in the foreground, then the report at line 675 |
| Runner | one call, iteration 1 (command-log line 391, logged once). No iteration 2 was needed. |
| After the runner | nothing. Checked after the export: the worktree's Java (9 files) and trace are byte-identical to the staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` and `.gitignore` edits, setup's removals (including `csp_overrides.csp`) and the reports |

**Verdict: valid.**

**Result: converged at iteration 1** at `56cb43d7`. All twelve phases pass: FDR4
3 of 3 assertions; Dafny 1 file, 7 verified, 0 errors; Isabelle 1 theory, 21 lemmas
(1 min 14 s); vacuity passed with two advisory findings (a state with no enabled
operation when the environment offers nothing; the obstacle threshold 0.5 rounded
to 1 in the CSP model). The profile check passes with one rule-8 warning.
`time/Clock.java` converts `nanoTime` to seconds with the literal `1.0e9`; it appears
in none of the 39 CSP files, the Isabelle theory, the `.rct`, the Dafny model or the
RoboChart model. No `MAX_VALUE` in the Java.
