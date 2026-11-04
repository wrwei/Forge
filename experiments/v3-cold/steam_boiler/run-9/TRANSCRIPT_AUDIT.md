# Cold v3, Steam Boiler pass 9 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run9-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 1,639 lines; 18 Bash and 5 Write calls
shown) and the session log (`scripts/session_audit_logs.sh`: 21 distinct commands, 7 Read
calls on files of the worktree; tool listing Bash 31, Read 14, Write 8, ToolSearch 1,
which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 123 responses |
| Harness | Claude Code 2.1.317 for all 200 records (the update from 2.1.314 is disclosed in cold pass 8) |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and two IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and its `README.md`, the five rule files (Read tool), the five layer prompts and the scaffold's Gradle files (shell), a listing of `docs/corpus/` and of the still-empty `forge.assets/corrections/`, its own Java and its scratchpad logs; no pipeline report opened |
| Other tools | one `ToolSearch` (`select:Monitor`), which loads a Claude Code tool's definition and reads no file |
| How it wrote | most Java by shell heredocs with the content inline (no copy of any file); a trace generator in Claude Code's scratchpad that reads only the session's own Java and the study's requirements |
| Writes | all before the runner (last write at transcript line 1502; the runner command at line 1587, backgrounded at line 1590; line 119 is the prompt's preflight check) |
| Runner | called once; afterwards the session read only the runner's console log in its scratchpad (log command 21) and stopped |
| After the export | the 11 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Nine phases pass. Counted under the profile check, its first
failing phase.

- **Profile check, class (c).** Two rule-4 errors, both on `double` parameters
  (`BoilerSensor.maxSteamVolume#rate`, `minSteamVolume#rate`), none on a field (scoring
  note of 3 November 2026).
- **FDR4, class (e).** Stopped by the pipeline's monitor at the 81,920 MB budget after
  682 s (peak 82,128 MB), no verdict. The model has 27 transitions and 41 variables.
- **Isabelle, class (e).** The deadlock-freedom proof timed out.

Dafny passes (11 verified); the vacuity audit passes.
