# Cold v3, Steam Boiler pass 8 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run8-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 607 lines; 20 Bash, 14 Write and 1
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 28 distinct
commands, no Read call; tool listing Bash 44, Write 17, Edit 2, ToolSearch 1, which
includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 117 responses |
| Harness (disclosed) | Claude Code **2.1.317** for all 201 records; every earlier Steam Boiler session of this re-collection ran on 2.1.314, so Claude Code updated itself between cold pass 7 and this pass. Disclosed as in the earlier version changes (2.1.311 to 2.1.312, 2.1.312 to 2.1.314); not a void condition |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and one IDE classpath warning naming the session's own Java and the neutral project; no skill invoked |
| Reads | all by shell: the study's requirements and its `README.md`, the five rule files, the five layer prompts, `docs/corpus/README.md` and lines 60-120 of the corpus's `Z_Machine.thy`, its own Java and its scratchpad logs; no pipeline report opened |
| Other tools | one `ToolSearch` (`select:Monitor`), which loads a Claude Code tool's definition and reads no file |
| Edits | one Update call (two Edit records, logged twice) on its own `PlantMonitor`, before the runner (last write at transcript line 517) |
| Runner | called once (transcript line 541, backgrounded at line 544; line 157 is the prompt's preflight check); afterwards the session read only the runner's own output in its scratchpad (log command 28) and stopped |
| After the export | the 13 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Eight phases pass. Counted under the profile check, its first
failing phase.

- **Profile check, class (c).** Two rule-4 errors, both on `double` parameters
  (`PlantMonitor.analyseLevel#outflowMin`, `#outflowMax`), none on a field (scoring note of
  3 November 2026).
- **FDR4, class (e).** Killed by signal 9 after 1,116 s with no verdict (peak seen by the
  monitor 79,113 MB). The system log records `memorystatus: killing largest compressed
  process _refines [28615] 77712 MB` at 16:41:21 UTC, so macOS's memory-pressure killer
  ended it before the monitor's budget fired. The model has 45 transitions and 17 variables.
- **Isabelle, class (e).** The deadlock-freedom proof timed out.
- **Vacuity audit, class (c).** Crashed in its own parser ("maximum recursion depth
  exceeded").

Dafny passes (11 verified).
