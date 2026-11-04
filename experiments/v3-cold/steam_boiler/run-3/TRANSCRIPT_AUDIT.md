# Cold v3, Steam Boiler pass 3 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (81,920 MB;
FDR4's disk store through `scripts/fdr4_paged.sh`, confirmed in the worktree's
`pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run3-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 1,073 lines; 22 Bash, 20 Write and 5
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 28 distinct
commands, 6 Read calls on files of the worktree; tool listing Bash 47, Read 12, Write 22,
Edit 10, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 162 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and two IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and the five rule files (Read tool), its own Java by shell, a plant simulation of its own Java in Claude Code's scratchpad; no pipeline report opened |
| Edits | ten Edit records (five edits, each logged twice) on its own `BoilerSensor` and `SteamBoilerController`, all before the runner (last edit at transcript line 947) |
| Runner | called once (transcript line 1023, backgrounded at line 1026; line 157 is the prompt's preflight check); afterwards the session read only the runner's own console output (log command 28) and stopped |
| After the export | the 15 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Ten phases pass. Counted under FDR4, its first failing phase.

- **FDR4, class (e).** Killed by signal 9 after 853 s with no verdict (peak seen by the
  monitor 81,199 MB). The system log records `memorystatus: killing largest compressed
  process _refines [89676] 80458 MB` at 22:17:26 UTC, so macOS's memory-pressure killer
  ended it before the monitor's budget fired. The model has 53 transitions and 38 variables.
- **Isabelle, class (e).** The deadlock-freedom proof timed out.

Dafny passes (5 s); the vacuity audit passes with two advisory findings.
