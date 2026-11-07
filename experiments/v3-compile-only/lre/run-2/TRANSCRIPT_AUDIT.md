# Compile-only v3, LRE run 2 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 610 lines) and the session log
(`scripts/session_audit_logs.sh`: 24 logged commands, no Read call; tool listing 34 Bash,
17 Write).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 104 responses |
| Harness | Claude Code 2.1.318 (179 records) |
| Session | one user turn; no IDE notice; no IDE diagnostics |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all by shell, all permitted: the study's inputs, CLAUDE.md and the prompt files, its own Java, the visible reports, the runner's output |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Writes | all before the runner (the closing "19 files" adds the summary to the 18 counted earlier; nothing written after the runner) |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (15 files, 1,010 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 3 of 3
assertions; Dafny 12 verified, 0 errors; Isabelle 47 lemmas (1 min 59 s); vacuity passed
with three advisory findings (modes and transitions unreachable only under the checked
CSP ranges; `cpaepsilon` rounded in the CSP model). All twelve phases pass, so the stop
point is verification-passing. The profile check passed with one rule-6 and nine rule-8
warnings.
