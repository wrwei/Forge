# Cold v3, Steam Boiler pass 2 — transcript audit

At the pin `56cb43d7`; set up under Amendments 3, 4, 7, 8, 9 and 10; collected under
Amendments 11 and 12.

Sources: `transcript.md` (Claude Code `/export`, 742 lines; 19 Bash and 18 Write calls
shown) and the session log (`scripts/session_audit_logs.sh`: 24 distinct commands, 6
Read calls; the tool listing, which counts duplicate log entries, gives Bash 37, Read 12,
Write 19, ScheduleWakeup 1; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Opus 5.5 (`claude-opus-5-5`) for all 137 responses; Claude Code 2.1.314 |
| Session | one user turn; no IDE notice; only the `superpowers` session-start hook, text unchanged (hash `c0b542fbe3cb`) |
| Reads | all permitted: the Steam Boiler requirements, the five rule files in `forge.assets/prompts/`, the layer prompts and their README in `forge.assets/vibe-coding-prompts/` (both folders listed as inputs); no pipeline report, no other study, no collection or staging folder |
| Writes | all before the runner (18 Write calls shown, the last before the runner command at transcript line 668) |
| Runner | called once (one distinct command), shown running in the background at transcript line 668 |
| Wake-up | one 30-minute fallback wake-up set while the runner ran, not cancelled; it never fired: the transcript ends at the session's `ITER_1_COMPLETE` line, and the log has no turn after it |
| After the export | the 15 Java files are byte-identical to the record; no source newer than the record; `git status` shows only setup's `.gitignore` and `pipeline.yaml` edits and the session's corrections files |

**Verdict.** Valid; not converged. Ten phases pass, Dafny verification included; FDR4 and
Isabelle fail. Java: 15 files, 1,285 lines; 42 transitions in the extracted model.

- **FDR4 (first failing phase; class (c), pipeline defect).** CSP type error after 1 s:
  `Couldn't match expected type Int=>InOut with actual type InOut ... In the expression:
  SteamBoilerController::openPump.out`. The Java sends `actuator.apply(new
  OutputEvent.OpenPump(sensor.fillPump()))`, an output event whose value is a sensor call;
  the extractor drops the value and emits `send openPump` (model lines 147 and 154) on an
  event declared `int`, while literal values (`openPump ! 1`) are extracted. No written
  rule restricts an output event's value to a literal.
- **Isabelle (class (c)).** Type error after 22 s: the boolean sensor call behind the named
  predicate `linkFailed` (`sensor.transmissionFailure()`) is declared `unit ⇒ real`, the
  Isabelle generator's fallback type, and used under `∨` (theory line 137); Isabelle's
  coercion inference fails. Section 4(a) of the rules allows predicates built from sensor
  calls.

The vacuity audit passes.
