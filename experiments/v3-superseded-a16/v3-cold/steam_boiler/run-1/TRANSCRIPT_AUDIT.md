# Cold v3, Steam Boiler pass 1 (Amendment 15 re-collection) — transcript audit

At the Amendment 14 pin `d714365c`; set up under Amendments 3, 4, 7, 8, 9 and 10; collected
under Amendments 11, 12, 14 and 15 (worktree `forge-v3c15-run1-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 706 lines; 14 Bash, 18 Write and 1
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 21 distinct
commands, no Read call; tool listing Bash 31, Write 21, Edit 4, ToolSearch 1, the Bash and
Edit counts including entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 122 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and two IDE classpath warnings naming the neutral project's `SteamBoilerProgram.java` and the session's own scratch file; no skill invoked |
| Reads | all by shell and permitted: the study's requirements, the rule files, its own Java; no pipeline report opened |
| Scratch work | a simulator of its own Java, compiled and run in Claude Code's scratchpad (`Sim.java`, two edits there); it reads no file of the worktree beyond the session's Java |
| Writes | all before the runner (last write at transcript line 567, the `ITER_SUMMARY.md`; the runner command at line 648) |
| Runner | called once (log command 21), in the background; the session stopped after its result |
| After the export | the 14 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Ten phases pass. Counted under FDR4, its first failing phase.

- **FDR4, class (c).** FDR4 evaluates the model and reports each of its three assertions
  with the error "The value: SteamBoilerController::openPump.out.2 is invalid ... not a
  member of the set {0, 1}" (raw output from the operator's terminal, 2 November). The Java
  sends the pump numbers as literals (`new OutputEvent.OpenPump(1)` ... `(4)`); the checked
  instantiation gives `int` the range {0, 1}, and the pipeline's range step widens it for
  named constants sent on events but not for literal values. The runner reports the three
  assertions as "inconclusive" with no issue, because each FDR4 result carries only an
  error. The rules allow literal output values.
- **Isabelle, class (c).** The Java record `PumpReading(int pump, boolean open)` becomes an
  Isabelle record with a field named `open`, an Isabelle keyword; the theory does not parse
  (line 51). The rules' reserved-word list (rule 3) names RoboChart keywords only.

Neither defect is in a path Amendment 14 changed. Dafny passes; the vacuity audit passes.

**Superseded (Amendment 16).** This session ran at the Amendment 14 pin `d714365c`; both of
its failures are defects of that pin, fixed by Amendment 16. Under the pin rule of
Amendment 15 it is superseded and the Steam Boiler re-collected; the record is kept here
and not counted.
