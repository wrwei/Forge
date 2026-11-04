# Cold v3, Steam Boiler pass 7 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run7-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 709 lines; 17 Bash and 18 Write calls
shown) and the session log (`scripts/session_audit_logs.sh`: 28 distinct commands, no
Read call; tool listing Bash 44, Write 19, ToolSearch 1, which includes entries Claude Code
logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 126 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | only the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`); no skill invoked |
| Reads | all by shell: the study's requirements and its `README.md`, the five rule files, the five layer prompts in `forge.assets/vibe-coding-prompts/`, `docs/corpus/README.md` and one corpus example (`DwarfSignal.thy`), its own Java and its scratchpad logs; no pipeline report opened |
| Other tools | one `ToolSearch` (`select:Monitor`), which loads a Claude Code tool's definition and reads no file |
| Scratch work | a script in Claude Code's scratchpad that writes the session's own Layer 5 report (`post_codegen.{json,md}`) |
| Writes | all before the runner (last write at transcript line 517; the runner command at line 629, backgrounded at line 632) |
| Runner | called once; afterwards the session read only the runner's own output and its console log (log commands 27-28) and stopped |
| After the export | the 16 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Nine phases pass. Counted under the profile check, its first
failing phase.

- **Profile check, class (a).** Eleven rule-4 errors in `CycleAnalyser`: six on `double`
  fields without `@RoboChartType("real")` (`CYCLE`, `levelReading`, `steamReading`,
  `previousSteam`, `levelLow`, `levelHigh`), which the rule requires, and five on `double`
  parameters.
- **FDR4, class (e).** Killed by signal 9 after 827 s with no verdict (peak seen by the
  monitor 72,031 MB). The system log records `memorystatus: killing largest compressed
  process _refines [14328] 71309 MB` at 14:31:25 UTC, so macOS's memory-pressure killer
  ended it before the monitor's budget fired. The model has 29 transitions and 32 variables.
- **Isabelle, class (e).** The deadlock-freedom proof timed out.

Dafny passes (11 verified); the vacuity audit passes.
