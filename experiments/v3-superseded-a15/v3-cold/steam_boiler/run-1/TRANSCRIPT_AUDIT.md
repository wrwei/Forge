# Cold v3, Steam Boiler pass 1 — transcript audit

At the pin `56cb43d7`; set up under Amendments 3, 4, 7, 8, 9 and 10; collected under
Amendments 11 and 12.

Sources: `transcript.md` (Claude Code `/export`, 440 lines, one user turn, 29 Bash calls
shown), the session log (`scripts/session_audit_logs.sh`: 67 logged Bash entries, 39
distinct call ids, no Read call), `scripts/session_tool_results.py` (each Bash call with
the start of its result), and the kernel log for 16:45-17:05.

| Check | Result |
|---|---|
| Model | Opus 5.5 (`claude-opus-5-5`) for all 139 responses; Claude Code 2.1.314 |
| Session | one user turn (the pasted prompt); no IDE notice; only the `superpowers` session-start hook, text unchanged (hash `c0b542fbe3cb`) |
| Reads | all permitted: the Steam Boiler inputs, the prompt and rule files, `docs/corpus/` (the Z-Machine examples, which the inputs allow); no pipeline report, no other study, no collection or staging folder |
| Writes | all before the runner; `ITER_SUMMARY.md` trimmed to 297 words before it, as the prompt requires |
| Runner | called once. The session log holds two entries for it (commands 65 and 66, the second with a `cd <worktree> &&` prefix), but both carry the same call id `toolu_011M9nwrvw8CT5DaJixoZz3o` and the same background task `b1mt7pi7z`: Claude Code logged the one call twice. The same doubling affects 28 of the 67 entries (39 distinct ids). `scripts/session_commands.py` now prints each call id once. |
| After the export | the 14 Java files are byte-identical to the record; no source newer than the record; `git status` shows only setup's `.gitignore` and `pipeline.yaml` edits and the session's corrections files |

**Verdict.** Valid; not converged. Nine phases pass (compile, coverage, profile check,
the five transformations, vacuity); FDR4, Dafny and Isabelle fail. Java: 14 files, 1,345
lines; 42 transitions in the extracted model.

- **FDR4 (first failing phase; class (e)).** Exit -9 after 772 s, reported by the runner as
  "CSP-M parse error". The kernel log shows `memorystatus: killing largest compressed
  process _refines [52780] 79346 MB` at 17:02:09: FDR4's own footprint exceeded the 48 GiB
  machine, as in iteration 3 of Reference run 1. Under the scoring note this is recorded
  as no verdict within the machine's memory, not as an external kill.
- **Dafny (class (c), pipeline defect).** `Ambiguous use of && and ||` at line 162 and four
  more. The Java guard `... && !valveOpen && (pumpStoppable || !pumpStartable)` is
  bracketed and passes the profile check; the Dafny generator prints Spoon's operator
  tree without restoring brackets, giving `... && !valveOpen && stoppablePumpAvailable()
  || !startablePumpAvailable()`. A scan of all 44 converged iterations of the collection
  finds no Dafny, Isabelle or RoboChart line mixing connectives at one bracket depth, so
  no reported verdict is affected.
- **Isabelle (cause not isolated).** `exception Empty raised (line 47 of
  "./basis/List.sml")` at the `zoperation` INITIALIZATIONToINITIALIZATION (theory line 140).
  The operation is the first in the collection whose precondition reads a state variable
  and whose `listens` update lists an event more than once (17 copies); each feature alone
  occurs in accepted theories (241 and 16 operations), the two together in none.

The vacuity audit passes with two advisories (10 transitions unfireable under the checked
CSP instantiation; one reachable state with no enabled operation when the environment
offers nothing).

**Note (added after cold pass 2's audit; the verdict and its count are unchanged).** The
likely cause of the Isabelle exception is a typing defect of the Isabelle generator that
cold pass 2 shows plainly: a boolean sensor method reached through a named predicate is
declared `unit ⇒ real` (here `boilerWaitingReceived`, a `boolean` method of
`BoilerSensor`, at theory line 62) and then used under `∨` in the precondition at line
143. In pass 2 Isabelle reports the same clash as a failed coercion; here the coercion step
raised `Empty` instead. Not confirmed by a re-run of Isabelle.
