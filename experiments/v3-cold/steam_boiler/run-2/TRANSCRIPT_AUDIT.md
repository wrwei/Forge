# Cold v3, Steam Boiler pass 2 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`; set up under Amendments 3, 4, 7, 8, 9 and 10;
collected under Amendments 11, 12, 14, 15 and 16 (worktree `forge-v3c16-run2-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 423 lines; 25 Bash and 3 Write calls
shown) and the session log (`scripts/session_audit_logs.sh`: 30 distinct commands, 7 Read
calls on files of the worktree; tool listing Bash 43, Read 14, Write 3, which includes
entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 124 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | only the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`); no skill invoked |
| Reads | the study's requirements and its `requirements/README.md` (both listed as inputs in the prompt), the five rule files, its own Java; no pipeline report opened |
| How it wrote | the Java by shell heredocs with the content inline (no copy of any file), three files by the Write tool; a smoke test and a scratch file in Claude Code's scratchpad |
| Writes | all before the runner (the runner command at transcript line 366, backgrounded at line 369; no write after it in the command log) |
| Runner | called once; the session then stopped without reading any report |
| Leakage gate | the only hits are in the pipeline's own reports on this session's model, which name its own study |
| After the export | the 13 Java files and the trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Not converged.** Nine phases pass. Counted under the profile check, its first
failing phase.

- **Profile check, class (a).** One error: the field `BoilerSensor.DT` is a `double`
  without `@RoboChartType("real")` (rule 4), which the rules require on every `double`
  field.
- **FDR4, class (e).** No verdict within the memory budget: the monitor stopped FDR4 at
  40,982 MB against the 40,960 MB budget after 297 s. The model has 34 transitions and 50
  variables.
- **Isabelle, class (e).** The deadlock-freedom proof did not finish within the pipeline's
  per-goal limit (595 s for the phase).

Dafny passes; the vacuity audit passes.
