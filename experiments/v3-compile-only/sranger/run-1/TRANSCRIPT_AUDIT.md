# Compile-only v3, SRanger run 1 — transcript audit

At the Amendment 18 pin `aaddf222`, the first session of the Compile-only condition
(review/prereg-v3-study-free.md, Amendment 18); set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 531 lines) and the session log
(`scripts/session_audit_logs.sh`: 21 logged commands, no Read call; tool listing
37 Bash, 12 Write).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 71 responses |
| Harness | Claude Code 2.1.318 (138 records); the first session on this version |
| Session | one user turn; no IDE notice |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all permitted: the study's inputs, CLAUDE.md and the prompt files, the layer prompts, its own Java (one grep over `src/main/java`), the eight visible reports |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Writes | all before the runner |
| Runner | called once (iteration 1) |
| Stop | the runner printed ALL VISIBLE PHASES PASSED; the session stopped and reported "stop point at iteration 1" |
| After the export | Java (9 files, 311 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 3 of 3
assertions; Dafny 7 verified, 0 errors; Isabelle 21 lemmas (42 s); vacuity passed with the
two advisory findings every SRanger session reports. All twelve phases pass, so the stop
point is verification-passing. The profile check passed with one rule-8 warning.
