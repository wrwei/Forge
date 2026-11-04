# Cold v3, Steam Boiler pass 6 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10 (worktree
`forge-v3c16-run6-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 785 lines; 21 Bash, 17 Write and 2
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 25 distinct
commands, 6 Read calls on files of the worktree; tool listing Bash 33, Read 12, Write 17,
Edit 4, ToolSearch 1, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 139 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and two IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and the five rule files (Read tool), its own Java by shell; no pipeline report opened |
| Other tools | one `ToolSearch` (`select:Monitor`), which loads a Claude Code tool's definition and reads no file; the Monitor tool was not then called |
| Scratch work | a script in Claude Code's scratchpad that writes the session's own Layer 5 report (`post_codegen.{json,md}`) |
| Edits | two Update calls (four Edit records, each logged twice) on its own `PumpUnit` and `SteamBoilerProgram`, all before the runner (last write at transcript line 627) |
| Runner | called once (transcript line 715, backgrounded at line 718); afterwards the session read only the runner's own output (log command 25) and stopped |
| After the export | the 15 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Nine phases pass, the profile check among them. Counted under
FDR4, its first failing phase.

- **FDR4, class (e).** Stopped by the pipeline's monitor at the 81,920 MB budget after
  705 s (peak 81,925 MB), no verdict. The model has 30 transitions and 39 variables.
- **Isabelle, class (e).** The deadlock-freedom proof timed out.
- **Vacuity audit, class (c).** Crashed in its own parser ("maximum recursion depth
  exceeded").

Dafny passes (11 verified).
