# Reference v3, Steam Boiler run 2 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10; no iteration
cap (worktree `forge-v3r16-run2-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 3,394 lines; 67 Bash, 23 Write and 19
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 82 distinct
commands, 12 Read calls on files of the worktree; tool listing Bash 112, Read 23, Write
29, Edit 38, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 375 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and eight IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and its `README.md`, the rule files, its own Java, `docs/corpus/` (a listing and its README at the start; at log command 1548 a count of `zoperation`s in the corpus's `deadlock_free` examples), and its own reports. All are inputs the pre-registration permits |
| Report reads (deviation, non-voiding) | after each iteration it read the reports of the failing phases, as the prompt says (runner calls at log commands 457, 674, 988, 1237, 1542, 2148). It also read reports of phases that had passed: the head of `post_vacuity.md` after iterations 3, 4 and 5 (log commands 990, 1241, 1546), and the head of `post_fdr4.md` after iteration 5 (log command 1546), whereas the prompt says to read "only" the failing phases' reports. The pre-registration permits a Reference run its own reports and voids only report reads in a single pass, so the run stands. Those passing reports carried one advisory vacuity finding (iteration 3) and none (iterations 4 and 5; FDR4's 3 of 3 in iteration 5) |
| Runner | six calls, one per iteration (transcript lines 837, 1996, 2486, 2766, 3062, 3271) |
| Writes | each iteration's changes before that iteration's runner call; none after the last (last edit at transcript line 3191) |
| After the export | the 6th iteration's Java and trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Converged at iteration 6.**

| Iteration | Transitions / variables | Failing phases (class) |
|---|---|---|
| 1 | 31 / 24 | profile check: one `double` parameter without `@RoboChartType("real")` (c, scoring note of 3 November 2026); FDR4 stopped at the 80 GiB budget, peak 82,152 MB (e); Isabelle deadlock-freedom timeout (e); vacuity audit crashed, "maximum recursion depth exceeded" (c) |
| 2 | 41 / 29 | FDR4 killed by signal 9 (e); Isabelle timeout (e) |
| 3 | 43 / 19 | FDR4 at the budget, peak 81,973 MB (e); Isabelle timeout (e) |
| 4 | 44 / 20 | FDR4 killed by signal 9 (e); Isabelle timeout (e) |
| 5 | 35 / 10 | Isabelle timeout (e); FDR4 passes in 5 s |
| 6 | 18 / 12 | none: FDR4 3 of 3 (4 s); Dafny 11 verified; Isabelle 49 lemmas (2 min 36 s); vacuity passed |

**What the converged model covers.** By its own summary, the controller decides the modes
and the initialization handshake; `SteamBoilerProgram` sends MODE, VALVE, PROGRAM_READY and
the pump orders and does the regulation; `UnitMonitor` does failure detection, unit status,
stop counting, level estimation and the failure and repair messages. It states that two
things which were in the model in earlier iterations are now Java outside it: "which
conditions demand emergency stop in which mode, and the pump regulation". Its sensor facts
were cut to four freely varying booleans before FDR4 finished.

**Notes the session made on the instrument** (none required a stop): the Isabelle fix
directive blamed modes without an event-only transition, but the timeout continued after
every mode had one, and it timed out at every model size from 29 to 43 and passed at 17;
the vacuity audit's crash in iteration 1.
