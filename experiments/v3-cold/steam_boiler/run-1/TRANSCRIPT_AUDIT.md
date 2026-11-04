# Cold v3, Steam Boiler pass 1 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`; set up under Amendments 3, 4, 7, 8, 9 and 10;
collected under Amendments 11, 12, 14, 15 and 16 (worktree `forge-v3c16-run1-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 2,244 lines; 21 Bash, 27 Write and 3
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 32 distinct
commands, 3 Read calls on files of the worktree; tool listing Bash 51, Read 6, Write 29,
Edit 14, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 174 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and six IDE classpath warnings, each naming one of the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements (Read tool, twice), the rule files and its own Java by shell, and its own simulator in Claude Code's scratchpad; no pipeline report opened |
| Scratch work | a simulator of its own Java (`Sim.java` in the scratchpad), edited and run there; it reads no file of the worktree beyond the session's Java |
| Writes | all before the runner (last write at transcript line 2151; the runner command at line 2192, backgrounded at line 2195). Transcript line 157 is the prompt's preflight check, which sets the same variables |
| Runner | called once (log command 31); afterwards the session read only the runner's own output file (log command 32) and stopped |
| After the export | the 17 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Nine phases pass. Counted under FDR4, its first failing phase.

- **FDR4, class (e).** No verdict within the memory budget: the pipeline's monitor (the
  physical footprint, Amendment 14) stopped FDR4 at 41,402 MB against the 40,960 MB budget
  after 348 s and reported a memory kill. The model has 43 transitions and 28 variables.
- **Dafny and Isabelle, class (b).** The Java declares two enums with the same value names,
  `OperatingMode` (the controller's modes) and `ModeValue` (the mode message), both with
  `INITIALIZATION`, `NORMAL`, `DEGRADED`, `RESCUE` and `EMERGENCY_STOP`. In the model the
  mode names and the enumeration values coincide; Dafny reports that `INITIALIZATION` "does
  not denote a datatype constructor uniquely", and Isabelle cannot type `ModeValue.DEGRADED`.
  The rules require that "Each name in the model must also be unique within its scope"
  (`java_codegen_rules.txt`, the naming section); the examples given there are about events.

The vacuity audit passes.
