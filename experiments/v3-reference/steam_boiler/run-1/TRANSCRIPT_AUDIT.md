# Reference v3, Steam Boiler run 1 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`; set up under Amendments 3, 4, 7, 8, 9 and 10;
collected under Amendments 11–16, no iteration cap (worktree `forge-v3r16-run1-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 2,435 lines; 43 Bash, 24 Write and 10
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 69 distinct
commands, 5 Read calls on files of the worktree; tool listing Bash 105, Read 8, Write 24,
Edit 20, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 303 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and two IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements, the rule files, its own Java (Read tool on its own controller and `PlantMonitor`), and between iterations only reports of phases that had failed (iteration 1: profile check, FDR4, Isabelle, vacuity; iteration 2: FDR4; iteration 3: Isabelle). Its summary notes that iteration 1's runner console showed a traceback naming a `forge.dashboard` file and that it did not open that file; the command log confirms no read under `forge.dashboard/` |
| Runner | four calls, one per iteration (log commands 422, 906, 1125, 1395; command 34 is the prompt's preflight `ls` of the runner's path) |
| Writes | each iteration's changes before that iteration's runner call; none after the last |
| After the export | the 4th iteration's Java and trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Converged at iteration 4.**

| Iteration | Transitions / variables | Failing phases (class) |
|---|---|---|
| 1 | 30 / 21 | profile check: 10 `double`s without `@RoboChartType("real")`, 8 of them fields (a; the 2 parameters alone would be (c)); FDR4 stopped at the 40 GiB budget (e); Isabelle deadlock-freedom timeout (e); vacuity audit crashed, "maximum recursion depth exceeded" in its own parser (c) |
| 2 | 42 / 19 | FDR4 at the budget (e); Isabelle timeout (e) |
| 3 | 43 / 14 | FDR4 at the budget (e); Isabelle timeout (e) |
| 4 | 20 / 11 | none: FDR4 3 of 3 (7 s); Dafny 11 verified; Isabelle 53 lemmas (3 min 3 s); vacuity passed with no finding |

**What the converged model covers.** At iteration 4 the session moved the per-cycle valve
and pump commands (29–42 transitions) out of the state machine into a separate Java class,
`PumpRegulator`, that `SteamBoilerProgram` calls after each step. Failure detection, the
acknowledgement protocol and the level estimate were already in `PlantMonitor`, whose
verdicts reach the controller as boolean sensor functions. Its summary states: "The
verified model now excludes detection, the acknowledgement protocol, the risk arithmetic
and the valve and pump commands." The verified controller holds the five modes, every mode
transition, the MODE broadcast and the PROGRAM_READY handshake; its only sends are
`mode` (value dropped by the extractor) and `programReady`. No rule requires output
commands to be issued from the controller (rule 4 states what the controller class
contains), so the run is within the rules.
