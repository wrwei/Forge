# Cold v3, Steam Boiler pass 4 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run4-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 483 lines; 18 Bash and 2 Write calls
shown) and the session log (`scripts/session_audit_logs.sh`: 28 distinct commands, 6 Read
calls on files of the worktree; tool listing Bash 45, Read 12, Write 2, which includes
entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 127 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | only the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`); no skill invoked |
| Reads | the study's requirements and the five rule files (Read tool), its own Java by shell; no pipeline report opened |
| How it wrote | the Java by shell heredocs with the content inline (no copy of any file); a smoke test and two generator scripts (`gen_trace.py`, `gen_review.py`) in Claude Code's scratchpad, which write the trace and the Layer 5 report and read only their arguments |
| Writes | all before the runner (last Write at transcript line 326; the runner command at line 422, backgrounded at line 425; line 158 is the prompt's preflight check) |
| Runner | called once; afterwards the session read only the runner's own console log in its scratchpad (log command 28) and stopped |
| After the export | the 15 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Eight phases pass. Counted under the profile check, its first
failing phase.

- **Profile check, class (c).** Four errors, all on `double` parameters of two
  `BoilerSensor` helpers (`detectLevelFailure`, `detectSteamFailure`) without
  `@RoboChartType("real")`. Rule 4 lists method parameters among the places the annotation
  may go but requires it only on every `double` field ("required; the profile checker
  reports a missing one as an error"), so the Java is within the instructions and the
  checker enforces more than the rule states (scoring note of 3 November 2026).
- **FDR4, class (e).** Killed by signal 9 after 561 s with no verdict (peak seen by the
  monitor 76,213 MB). The system log records `memorystatus: killing largest compressed
  process _refines [22928] 75979 MB` at 04:15:04 UTC, so macOS's memory-pressure killer
  ended it before the monitor's budget fired. The model has 39 transitions and 36 variables.
- **Isabelle, class (c).** The generated theory has empty right-hand sides for the
  rescue-mode level estimates (`levelLow′ =`, `levelHigh′ =`) in five operations
  (`NORMALToRESCUE`, `DEGRADEDToRESCUE`, three `RESCUEToRESCUE`), while the RoboChart model
  carries the full expressions (e.g. `levelLow = levelLow + cycleSeconds * pumpThroughput -
  cycleSeconds * steamReading - steamRiseMargin`); Isabelle reports an inner syntax error.
  A defect of the Isabelle generator, not yet diagnosed.
- **Vacuity audit, class (c).** Crashed in its own parser ("maximum recursion depth
  exceeded"), as in Reference run 1, iteration 1.
