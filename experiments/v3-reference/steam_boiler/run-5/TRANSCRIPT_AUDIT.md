# Reference v3, Steam Boiler run 5 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10; no iteration
cap (worktree `forge-v3r16-run5-steam_boiler`). The last session of the collection.

Sources: `transcript.md` (Claude Code `/export`, 2,148 lines, not compacted; 46 Bash, 25
Write and 9 Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 61
distinct commands, 2 Read calls on files of the worktree plus one on its own Java; tool
listing Bash 81, Read 3, Write 26, Edit 18, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 255 responses; Claude Code 2.1.317 for all 417 records (the update from 2.1.314 is disclosed in cold pass 8) |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and four IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements, the rule files and layer prompts (shell), the scaffold's Gradle files, its own Java and scratchpad logs, the runner's console output, and only reports of failing phases: after iteration 1 `post_preflight.md`, `post_fdr4.md`, `post_isabelle_verify.md`, `post_vacuity.md` (log command 337); after iteration 2 `post_fdr4.md`, `post_isabelle_verify.md` (log commands 629, 631); after iteration 3 `post_isabelle_verify.md`, the head of `post_fdr4.md` (log command 747) |
| Directory listings | at log command 28, `ls` of `forge.assets/corrections` (empty), `forge.assets/corrections-rtl` (tracked at the pin; holds only a `.gitignore`) and `docs/` (folder names `corpus` and `superpowers`); no file in `docs/` outside `docs/corpus/` was opened. The runner's console output after iteration 1 printed a traceback naming a file under `forge.dashboard/web/`, and one Isabelle report named a path under `docs/fixes/`; the session opened neither |
| Runner | four calls, one per iteration (log commands 333, 625, 743, 1039) |
| Writes | each iteration's changes before that iteration's runner call; none after the last (last write at transcript line 2011) |
| After the export | the 4th iteration's Java and trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Converged at iteration 4.**

| Iteration | Transitions / variables | Failing phases (class) |
|---|---|---|
| 1 | 30 / 17 | profile check: the `double` field `BoilerSensor.CYCLE` without `@RoboChartType("real")` (a); FDR4 at the 80 GiB budget, peak 82,020 MB (e); Isabelle deadlock-freedom timeout (e); vacuity audit crashed, "maximum recursion depth exceeded" (c) |
| 2 | 42 / 15 | FDR4 at the budget, peak 81,973 MB (e); Isabelle timeout (e) |
| 3 | 42 / 15 | FDR4 at the budget, peak 82,021 MB (e); Isabelle timeout (e) |
| 4 | 30 / 12 | none: FDR4 3 of 3 (778 s); Dafny 11 verified; Isabelle 73 lemmas (7 min 0 s); vacuity passed |

**What the converged model covers.** By the session's report, failure detection, per-unit
status, the level estimate and forecasts, and the failure/repair message protocol are in
`BoilerSensor` and `SteamBoilerProgram`, outside the extracted model; the verifiers see 8
boolean judgements from the sensor (the emergency-stop conditions combined into one per
phase). Of the five Reference runs, this is the largest controller FDR4 verified (runs 1-4: 20, 18, 17 and 17 transitions) (30
transitions, at 12 variables, in 778 s). The session reports that its guards rely on
else-if priority with a catch-all last branch per mode, so the extracted model lets a
mode's transitions overlap where the Java has a strict order (within rule 4(b), every
branch testing the event), and that between iterations 1 and 4 it changed the reading of
SB-Beh22/SB-Beh24 (MODE(EMERGENCY_STOP) now sent every cycle).
