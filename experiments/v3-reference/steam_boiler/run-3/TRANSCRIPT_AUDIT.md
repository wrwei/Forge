# Reference v3, Steam Boiler run 3 (Amendment 16 re-collection) — transcript audit

At the Amendment 16 pin `aaddf222`, with Amendment 17's FDR4 settings (confirmed in the
worktree's `pipeline.yaml`); set up under Amendments 3, 4, 7, 8, 9 and 10; no iteration
cap (worktree `forge-v3r16-run3-steam_boiler`).

Sources: `transcript.md` (Claude Code `/export`, 2,810 lines; 42 Bash, 31 Write and 6
Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 50 distinct
commands, 10 Read calls on files of the worktree; tool listing Bash 64, Read 16, Write 33,
Edit 12, ToolSearch 1, which includes entries Claude Code logged twice).

| Check | Result |
|---|---|
| Model | `claude-opus-5-5` for all 269 responses; Claude Code 2.1.314 |
| Operator input | one user turn (the prompt); no IDE notice |
| Hooks and skills | the `superpowers` session-start hook (text unchanged, sha256 prefix `c0b542fbe3cb`) and ten IDE classpath warnings naming the session's own Java files and the neutral project; no skill invoked |
| Reads | the study's requirements and the five rule files (Read tool), its own Java, the runner's console output, and only the reports of failing phases: after iteration 1 `post_preflight.md`, `post_fdr4.md`, `post_isabelle_verify.md` (log command 304); after iteration 2 `post_fdr4.md`, `post_vacuity.md` (log command 506) |
| Other tools | one `ToolSearch` (`select:Monitor`), which loads a Claude Code tool's definition and reads no file |
| Runner | three calls, one per iteration (log commands 300, 502, 844; transcript lines 765, 1765, 2740; line 172 is the prompt's preflight check) |
| Writes | each iteration's changes before that iteration's runner call; none after the last (last write at transcript line 2709) |
| After the export | the 3rd iteration's Java and trace are byte-identical to the record; no source file newer than the record; `git status` shows only setup's edits (`.gitignore`, `pipeline.yaml`) and the session's corrections files |

**Valid. Converged at iteration 3.**

| Iteration | Transitions / variables | Failing phases (class) |
|---|---|---|
| 1 | 35 / 17 | profile check: one `double` parameter (`PlantMonitor.withinCapacity#litres`) without `@RoboChartType("real")` (c, scoring note of 3 November 2026); FDR4 killed by signal 9 (e); Isabelle deadlock-freedom timeout (e) |
| 2 | 29 / 14 | FDR4 killed by signal 9 (e); vacuity audit crashed, "maximum recursion depth exceeded" (c) |
| 3 | 17 / 6 | none: FDR4 3 of 3 (2 s); Dafny 11 verified; Isabelle 45 lemmas (1 min 58 s); vacuity passed with one advisory finding |

**What the converged model covers.** The session's report states that FDR4 and Isabelle
check "only the mode transitions and the MODE broadcast"; level regulation (moved at
iteration 3 into a Java `LevelRegulator`), failure detection (FR1-FR5) and the
announce/acknowledge protocol (FR6/FR7) are Java outside the extracted model, checked only
by its own scratch simulations.

**A further note from the session.** Its guards rely on else-if order: every branch tests
the event, and the last branch of each mode is the event test alone. Rule 4(b) requires
conjoining earlier guards' negations only below branches "guarded only by predicates (no
event test)", so the Java is within the rules; the session notes that the extracted model
treats a mode's branches as alternatives and so allows more behaviour than the Java (for
example, staying in a mode is always possible in the model).
