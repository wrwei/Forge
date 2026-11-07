# Compile-only v3, SRanger run 5 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 487 lines) and the session log
(`scripts/session_audit_logs.sh`: 19 logged commands, no Read call; tool listing 30 Bash,
11 Write, 2 ScheduleWakeup).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 84 responses |
| Harness | Claude Code 2.1.318 (149 records) |
| Session | one user turn; no IDE notice; no IDE diagnostics |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all by shell, all permitted: the study's inputs, CLAUDE.md and the prompt files, its own Java, a listing of the corrections directory before the runner (it held only the session's own files), the runner's output |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Wake-up calls | one 30-minute fallback set while the runner ran in the background, then cancelled; neither reads anything |
| Writes | all before the runner (transcript line 401): the 9 Java files, the trace, `post_codegen` and the summary; nothing written after the runner |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (9 files, 258 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 3 of 3
assertions; Dafny 7 verified, 0 errors; Isabelle 21 lemmas (27 s); vacuity passed with
the two advisory findings every SRanger session reports. All twelve phases pass, so the
stop point is verification-passing. The profile check passed with one rule-8 warning.
