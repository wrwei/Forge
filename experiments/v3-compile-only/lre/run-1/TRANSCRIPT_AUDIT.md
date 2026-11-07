# Compile-only v3, LRE run 1 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 681 lines) and the session log
(`scripts/session_audit_logs.sh`: 19 logged commands, 11 Read calls; tool listing
25 Bash, 11 Read, 19 Write, 2 ScheduleWakeup).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 112 responses |
| Harness | Claude Code 2.1.318 (190 records) |
| Session | one user turn; no IDE notice |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all permitted: the requirements, the five prompt files (each read twice), its own Java, `post_codegen.md`, and the runner's own terminal output (the runner ran as a background task; its output file holds only the eight visible phase statuses) |
| Withheld reports | never named as files in the transcript or the command log; absent from the worktree after the runner. The session's closing summary notes that the runner withholds verifier output, which the runner's banner states in this mode; it saw no verdict |
| Wake-up calls | one 30-minute fallback reminder set while the runner ran in the background, then cancelled; neither reads anything |
| Writes | all before the runner (18 files at ITER_1_COMPLETE; 0 after) |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (15 files, 808 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 3 of 3
assertions; Dafny 12 verified, 0 errors; Isabelle FAILED (type error in
`LreController_Beh`: `momentrymaxvel` declared `unit ⇒ real` and used as `real`);
vacuity passed with three advisory findings. The profile check passed with one rule-6
and nine rule-8 warnings.

**Classification of the Isabelle failure: pipeline defect on Java within the
instructions.** The Java compares `calcVelOp.vel() <= LreConstants.momEntryMaxVel`, a
`real`-annotated constant. The Isabelle generator renders the constant as the call
`momentrymaxvel()` and declares it as a function, then strips the parentheses from
state-variable reads with a regex (`vel\(\)` → `vel`) that has no word boundary, so the
tail of `momentrymaxvel()` is also stripped and the term is ill-typed. The constant
`momentrymindist()` in the same guard, which ends in no state-variable name, is
unaffected. No collected session's theory contains the pattern. Disposition (operator,
5 November): recorded as is; the collection continues at the same pin. The defect is
described in `docs/defects/2026-11-05-thy-paren-strip-word-boundary.md`.

**Re-scored (Amendment 19, 6 November 2026).** The recorded iteration-1 Java and trace
replayed through all twelve phases at the Amendment 19 pin `1ad9301e`
(`rescore-a19/iter-1/`): all passed, Isabelle included (47 lemmas). The recorded verdict at
`aaddf222` (Isabelle failed on the generator defect) stays in `iter-1/`; under the
re-scoring rule of Amendment 19 the re-scored verdict is the one the analysis counts, so
this stop point is verification-passing.
