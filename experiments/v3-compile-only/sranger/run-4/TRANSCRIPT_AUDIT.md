# Compile-only v3, SRanger run 4 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 480 lines) and the session log
(`scripts/session_audit_logs.sh`: 15 logged commands, 5 Read calls listed twice; tool
listing 22 Bash, 10 Read, 10 Write).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 74 responses |
| Harness | Claude Code 2.1.318 (134 records) |
| Session | one user turn; no IDE notice; no IDE diagnostics |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all permitted: the five prompt files (Read, each twice), the requirements and the other inputs by shell, its own Java |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Writes | all before the runner (the closing "13 files" adds the summary to the 12 counted earlier; nothing written after the runner) |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (9 files, 294 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 3 of 3
assertions; Dafny 7 verified, 0 errors; Isabelle 21 lemmas (28 s); vacuity passed with
the two advisory findings every SRanger session reports. All twelve phases pass, so the
stop point is verification-passing. The profile check passed with one rule-8 warning.
