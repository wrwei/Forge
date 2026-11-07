# Compile-only v3, SRanger run 2 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4. Two iterations.

Sources: `transcript.md` (Claude Code `/export`, 542 lines) and the session log
(`scripts/session_audit_logs.sh`: 18 logged commands, 6 Read calls listed twice; tool
listing 27 Bash, 12 Read, 3 Write, 2 Edit, 1 ScheduleWakeup).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 85 responses |
| Harness | Claude Code 2.1.318 (146 records) |
| Session | one user turn; no IDE file-open notice |
| Hooks | `superpowers` SessionStart, text unchanged; one `ide_diagnostics` attachment from VS Code's Java language server on the session's own `Sensor.java` (classpath warning), carrying nothing from the pipeline or another study; non-voiding deviation as in Chemical Detector run 1 |
| Reads | all permitted: the requirements, the five prompt files (each twice), its own Java, and after iteration 1 the runner's output and `post_preflight.md`, the one visible report that failed |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after each runner call |
| Wake-up call | one 30-minute fallback set while iteration 1 ran in the background; cancelled; reads nothing |
| Writes | iteration 1: Java by shell heredoc, trace, `post_codegen`, summary, all before the first runner call; between the runner calls: one `Edit` adding `@RoboChartType("real")` to `Sensor.NO_READING_DISTANCE` (the second Edit record is the same edit, logged twice), `post_codegen` and the summary updated; nothing after the second runner call |
| Runner | called twice (iterations 1 and 2) |
| Stop | iteration 2 printed ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 2 |
| After the export | Java (9 files, 299 lines) and `result_codegen.json` byte-identical to the iteration-2 record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 2.**

Iteration 1: the profile check failed (rule 4, a `double` constant without
`@RoboChartType("real")`), with one rule-8 warning; every other visible phase passed.
Withheld verdicts at iteration 1 (recorded, not shown): FDR4 3 of 3; Dafny 7 verified,
0 errors; Isabelle 21 lemmas (29 s); vacuity passed with two advisory findings.
Iteration 2: all eight visible phases pass. Withheld verdicts at the stop point: FDR4 3
of 3; Dafny 7 verified, 0 errors; Isabelle 21 lemmas (7 s); vacuity passed with the same
two advisories. The stop point is verification-passing. The only change between the
iterations is the one annotation.
