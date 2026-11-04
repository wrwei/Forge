# Reference v3, Steam Boiler run 4 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10; no iteration
cap (worktree `forge-v3r16-run4-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 5,245 lines; 50 Bash, 37 Write and 14
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 65 distinct
commands, 12 Read calls; tool listing Bash 88, Read 19, Write 44, Edit 28, ScheduleWakeup
7, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 350 responses |
| Harness (disclosed) | Claude Code 2.1.317 for all 587 records, as in cold pass 8 (the update from 2.1.314 is disclosed there) |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and 13 IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and its `README.md`, the rule files, its own Java and scratchpad files, `docs/corpus/` (parts of `Z_Machine.thy` and `DwarfSignal.thy`), the runner's console output, and only reports of failing phases: after iteration 1 `post_preflight.md`, `post_isabelle_verify.md`, `post_vacuity.md` (log commands 99-105); after iterations 2 and 3 `post_isabelle_verify.md` (log commands 263, 371). The runner's console output includes a Python traceback naming files under `forge.dashboard/`; the session opened none of them, nor any generated artefact |
| Wake-ups | seven `ScheduleWakeup` calls: fallback reminders while a runner was in the background, the last a stop; none reads anything |
| Runner | five calls, one per iteration (log commands 95, 257, 367, 470, 608; transcript lines 1828, 2706, 3257, 3726, 5128; line 172 is the prompt's preflight check) |
| Writes | each iteration's changes before that iteration's runner call; none after the last (last write at transcript line 5032) |
| After the export | the 5th iteration's Java and trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Converged at iteration 5.**

| Iteration | Transitions / variables | Failing phases (class) |
|---|---|---|
| 1 | 29 / 46 | profile check: two `double` parameters without `@RoboChartType("real")`, no field (c, scoring note of 3 November 2026); FDR4 killed by signal 9 (e); Isabelle deadlock-freedom timeout (e); vacuity audit crashed, "maximum recursion depth exceeded" (c) |
| 2 | 28 / 47 | FDR4 at the 80 GiB budget, peak 82,174 MB (e); Isabelle timeout (e) |
| 3 | 28 / 47 | FDR4 at the budget, peak 82,062 MB (e); Isabelle timeout (e) |
| 4 | 17 / 45 | FDR4 at the budget, peak 82,568 MB (e) |
| 5 | 17 / 15 | none: FDR4 3 of 3 (9 s); Dafny 11 verified; Isabelle 45 lemmas (6 s); vacuity passed with one advisory finding |

**What the converged model covers.** The session's report states that from iteration 4
the valve, pump and PROGRAM_READY commands are issued outside the extracted state machine,
so the model verifies the mode transitions and the MODE message but not the commands
behind SB-Beh4, Beh5, Beh7, Beh10, Beh15 and Beh19; it checked them only with a scratch
simulation. Iteration 4 (17 transitions) still exhausted FDR4's memory with 45 variables;
FDR4 passed only after iteration 5 cut the sensor's and actuator's public interface
(list-, record- and count-valued methods) to 15 variables.

**Notes the session made on the instrument** (none required a stop): the vacuity audit's
crash in iteration 1; `ITER_SUMMARY.md` was written before iteration 5's verdict and still
lists FDR4 as pending.
