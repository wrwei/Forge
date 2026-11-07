# Compile-only v3, Chemical Detector run 5 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 456 lines) and the session log
(`scripts/session_audit_logs.sh`: 22 logged commands, 2 Read calls listed twice; tool
listing 22 Bash, 4 Read, 1 Write, 1 ToolSearch).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 89 responses |
| Harness | Claude Code 2.1.318 (139 records) |
| Session | one user turn; no IDE notice; no IDE diagnostics |
| Hooks | `superpowers` SessionStart only; text unchanged |
| Reads | all permitted: the requirements and the codegen rules (Read, each twice), the other inputs and layer prompts by shell, its own Java, the runner's output |
| Harness tool lookup | one `ToolSearch` for a `Monitor` tool while the runner ran in the background; a lookup of the harness's own tool list, reading no file |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Writes | all before the runner (transcript line 364): the Java by shell heredoc, the trace, `post_codegen` and the summary; nothing written after the runner |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (18 files, 726 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 9 of 9
assertions; Dafny FAILED (2 files, 0 verified: `unresolved identifier: values` in each);
Isabelle 2 theories, 78 lemmas (3 min 43 s); vacuity passed with one advisory per
controller. The stop point is verification-failing. The profile check passed with two
rule-8 warnings.

**Classification of the Dafny failure: pipeline defect on Java within the instructions
(the paper's class (c)).** `GasAnalysisSensor` declares
`private static final Angle[] DIRECTIONS = Angle.values();`, used only inside the sensor's
own methods and never referenced by a controller. The Dafny generator emits every
`static final` of every class as a `const` of default type `real`, so it wrote
`const DIRECTIONS: real := values()`, which Dafny cannot resolve. The constants rule
concerns numeric constants, and arrays and `Enum.values()` are not excluded features.
Disposition (operator's ruling of 5 November for defects found in this collection):
recorded as is; the collection ran at the same pin. The defect is described in
`docs/defects/2026-11-05-dafny-nonnumeric-static-final.md`.

**Re-scored (Amendment 19, 6 November 2026).** The recorded iteration-1 Java and trace
replayed through all twelve phases at the Amendment 19 pin `1ad9301e`
(`rescore-a19/iter-1/`): all passed, Dafny included (23 verified, 0 errors). The recorded
verdict at `aaddf222` (Dafny failed on the generator defect) stays in `iter-1/`; under the
re-scoring rule of Amendment 19 the re-scored verdict is the one the analysis counts, so
this stop point is verification-passing.
