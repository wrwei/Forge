# Cold v3, Steam Boiler pass 5 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run5-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 2,010 lines; 26 Bash, 19 Write and 3
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 29 distinct
commands, 7 Read calls on files of the worktree; tool listing Bash 41, Read 14, Write 21,
Edit 6, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 166 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and four IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and its `README.md`, the rule files, `docs/corpus/` (its README, file list and the deadlock definitions in `Z_Machine.thy`), its own Java; no pipeline report opened |
| Scratch work | a trace generator, a smoke test (a plant simulation of its own Java) and a guard check in Claude Code's scratchpad; they read only the session's own Java and the study's requirements |
| Edits | three Update calls (six Edit records, each logged twice) on its own `BoilerSensor`, all before the runner (last write at transcript line 1931) |
| Runner | called once (transcript line 1953, backgrounded at line 1956; line 158 is the prompt's preflight check); afterwards the session read only the runner's console log in its scratchpad (log command 29) and stopped |
| After the export | the 14 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Nine phases pass. Counted under the profile check, its first
failing phase.

- **Profile check, class (c).** Five errors, all on `double` parameters of `BoilerSensor`
  helpers without `@RoboChartType("real")` (for example `clamp#litres`,
  `detectLevelFailure#expectedLow`); no field is affected (the session's self-review had
  annotated the `double` field `DT`). Classed as in cold pass 4 (scoring note of
  3 November 2026).
- **FDR4, class (e).** Killed by signal 9 after 767 s with no verdict (peak seen by the
  monitor 74,544 MB). The system log records `memorystatus: killing largest compressed
  process _refines [76683] 72621 MB` at 10:46:37 UTC, so macOS's memory-pressure killer
  ended it before the monitor's budget fired. The model has 29 transitions and 48 variables.
- **Isabelle, class (e).** The deadlock-freedom proof timed out.

Dafny passes; the vacuity audit passes.
