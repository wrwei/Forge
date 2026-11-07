# Compile-only v3, Chemical Detector run 2 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 826 lines) and the session log
(`scripts/session_audit_logs.sh`: 21 logged commands, no Read call; tool listing 35 Bash,
26 Write, 2 Edit, 2 ScheduleWakeup).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 116 responses |
| Harness | Claude Code 2.1.318 (200 records) |
| Session | one user turn; no IDE notice; no IDE diagnostics |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all by shell, all permitted: the study's inputs, CLAUDE.md and the prompt files, its own Java, the visible reports, the runner's output |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Wake-up calls | one 30-minute fallback set while the runner ran in the background, then cancelled; neither reads anything |
| Writes | all before the runner, including the one `Edit` of `ITER_SUMMARY.md` (a wording change, logged twice) |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (21 files, 738 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 FAILED
(CSP-M type error, no assertion checked); Dafny 2 files, 23 verified, 0 errors; Isabelle 2
theories, 78 lemmas (4 min 1 s); vacuity passed with one advisory per controller. The
stop point is verification-failing. The profile check passed with two rule-8 warnings.

**Classification of the FDR4 failure: Java outside the instructions that no check before
the verifiers detects (the paper's class (b)).** Two of the four operation classes have a
`compute` method with parameters (`ChangeDirection.compute(Loc)`,
`Move.compute(double, Angle)`), against the rule that an operation class has one public
`void compute()` method. The profile checker does not test that rule; the extractor named
every operation after its method, `compute`, and merged them, so the generated CSP uses the
event `compute : Loc` both with and without a value and FDR4 cannot type it. This is the
same construct, class and failure as Chemical Detector Reference run 4, iteration 1
(Section 4.6 of the paper), which the Reference loop repaired at iterations 2 and 3 from
the FDR4 and Isabelle reports. Here those reports were withheld, so the run stopped.
