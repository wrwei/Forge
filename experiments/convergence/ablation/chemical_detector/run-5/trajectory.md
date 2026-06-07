# chemical_detector — ablation run-5 (compile-only) — trajectory

- **Condition:** `compile-only-ablation` (LAUNCH.md §B): only the eight
  compile-and-extraction feedback files were readable during the loop;
  `post_fdr4`, `post_dafny_verify`, `post_isabelle_verify`, `post_vacuity`
  and `summary.json` were withheld until after the stop point.
- **Actor:** me-as-developer (no sub-agents).
- **Stop point:** **iter-1** — all eight visible phases passed on the first
  pipeline run, so the compile-only stop rule fired immediately. No
  visible-feedback iterations were needed; no fixes were applied.

## Headline

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | pipeline wall-clock |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|---------------------|
| 1 | cold codegen (17 Java files + result_codegen.json) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 632.6 s (610.2 s of it isabelle_verify, a withheld phase) |

Withheld-verifier outcomes at the stop point are recorded deterministically
in `ablation_result.json` (emitted by `record_ablation_result.py`), not
narrated here from memory.

## Iter-by-iter

### Iter 1 — cold codegen

Read only the canonical inputs (RUN_TRAJECTORY §1): the chemical_detector
`system_description.txt` + `requirement_all.json`, CLAUDE.md, the three
codegen prompt files, and the process docs. Additionally read pipeline
*tooling* sources (allowed — the tool, not the answer key):
`forge.dashboard/web/feedback/coverage.py` (to learn the
`result_codegen.json` schema) and
`forge.transformations/.../preflight/StructuralLinter.java` (to learn the
lint rules, e.g. every `double` field/param needs `@RoboChartType("real")`).

Produced `chemdetector` (568 LOC, 17 files + trace):

- Two mode-nested if-else controllers per CD-ARCH2:
  `GasAnalysisController` (Reading/Analysis/NoGas/GasDetected/Stopped) and
  `MovementController` (Waiting/Going/Found/Avoiding/TryingAgain/
  AvoidingAgain/GettingOut).
- **No `Final` states** (CLAUDE.md Isabelle guidance): the GA terminal
  transition (CD-GA-Beh6) emits `stop` and reroutes to a live `Stopped`
  mode with an event-triggered bare-precondition self-loop (gas); MV
  `Found` self-loops on `stop`.
- **Total guard covers on autonomous-only modes** (CLAUDE.md trilemma
  guidance, no self-loops added): Analysis splits exhaustively on the
  two-valued Status enum (`sts == noGas` / `sts == gasD`); GasDetected on
  `goreq(ins, thr)` / its negation; AvoidingAgain on `makingProgress` /
  `!makingProgress`. NoGas has a bare autonomous transition to Reading.
- Sealed `InputEvent` (Gas, Obstacle, Turn, Stop, Resume) / `OutputEvent`
  (Turn, Stop, Resume, Flag) hierarchies; Turn/Stop/Resume present in both
  so the ETL can match the inter-controller (Shared) events by simple name.
- `ChemSensor` functions analysis/intensity/location/goreq/angle (CD-Fn1..4),
  `MoveSensor.odometer()`, `Vehicle` ops move/randomWalk/shortRandomWalk/
  changeDirection/pause (CD-OP1..4; `pause` is the ETL-recognised wait
  primitive), `Clock` + `evasionStartMs` field for the CD-MV-Clock1 evasion
  timer with the `nowMs() - field < CONST` predicate form.
- `result_codegen.json` mapping all 81 requirement IDs to Java elements.

Pipeline run: all eight visible phases **passed** → stop rule fired.
Loop ended with zero feedback-driven fixes.

## Caveats

- **Invented defaults** (spec silent): a reading "indicates the target
  chemical" iff some sample's `c` equals a configured target `Chem`
  (intensity not consulted); sensor-index→Angle mapping is
  0→Left, 1→Right, 2→Back, ≥3→Front; `intensity()` returns 0.0 on an empty
  reading (safe-default rule); constants all set to 1 / 1.0 (within the
  FDR4 `[0..1]` type ranges).
- CD-MV-FR1 wants `randomWalk()` as a *during* action; the extraction
  supports entry actions only, so it is the top-of-mode statement in
  Waiting (becomes an entry action in the model).
- CD-GA-Beh6 says GA "concludes its search, processing no further
  readings"; the live `Stopped` mode *receives* further gas events but
  discards them (required by the no-Final-state rule).
- Local FDR4 tuning applied per §C (timeout 3600, memory_limit_mb 65536)
  — not committed.

## Independence

- Fresh session in a severed-history worktree (single base commit, no
  run-archive ancestry). No auto-memory recall and no `.remember` hook
  content was injected at session start; the session had no prior
  knowledge of any chemical_detector trajectory and drove no other run.
- Iter-1 read only the canonical inputs plus pipeline tooling sources
  (listed above). No path under `docs/`, no `run-*`/`iter-*`, no findings
  KB, no ablation README was opened.
- **Leakage incident (disclosed):** while polling the *runner's console
  log* for progress during the iter-1 pipeline run, the tail of the log
  printed the FDR4 error excerpt (a CSP type error naming
  `insAtOrAboveThr :: Int`) and a truncated first line of the
  dafny_verify error before reading stopped. This was stdout from the
  runner itself, not a read of the withheld `post_*` files. It could not
  have influenced the run: the stop rule had already fired at iter-1 (all
  eight visible phases passed on the same run), so no subsequent fix
  existed for the leaked information to contaminate. Future ablation runs
  should poll the runner log filtered to `^=====` phase-boundary lines
  only (as was done here after the incident).

## Run-level cost

- **Pipeline wall-clock (iter-1, all 12 phases):** 632.6 s — dominated by
  `isabelle_verify` (610.2 s, withheld phase); the eight visible phases
  together took ~20 s.
- **End-to-end session span (setup → archive):** ≈ 35 min
  (2026-06-06 ~22:25 → ~23:00), of which ~10.5 min pipeline; the
  remainder is codegen, schema/tooling reading, and this write-up.
- **Claude token total:** not retrievable from inside the session (`/cost`
  is not exposed as a tool); the human can recover it from `/cost` or the
  session transcript JSONL per RUN_TRAJECTORY §6.2a.

## Findings

- **F1 — Compile-and-extraction phases are a weak filter for this study's
  shape.** A single careful cold codegen pass (controllers shaped by
  CLAUDE.md's inline structural guidance: no Final states, total guard
  covers, named predicates, annotation discipline) cleared all eight
  visible phases at iter-1. Whatever the withheld verifiers report (see
  `ablation_result.json`), none of it was surfaced by the visible
  pipeline — i.e. for this study the extraction phases validate *shape*,
  not *behaviour*. How to apply: do not treat "extraction green" as
  evidence of verifier-readiness.
- **F2 — Reading the feedback *generators* up front prevents whole
  iterations.** Two tooling reads (the coverage feedback module for the
  `result_codegen.json` schema; the structural linter for its four
  rules) let iter-1 pass coverage and preflight cold. These are
  tool-not-answer-key reads permitted by RUN_TRAJECTORY §1, and they are
  cheap insurance against burning an iter on a schema guess.
- **F3 — The runner's stdout leaks withheld verdicts in an ablation run.**
  `run_experiment_iteration.py` prints each phase's error excerpts to the
  console, including the withheld verifier phases. An ablation session
  monitoring progress must filter the log to phase-boundary lines
  (`grep -E '^====='`) instead of reading the raw tail.

## Reproducibility

Stage `run-5/iter-1/java/` into
`java.generated.project/src/main/java/chemdetector/`, copy
`run-5/iter-1/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set
`pipeline.yaml agent.active_case_study: chemical_detector`, then run
`python experiments/scripts/run_experiment_iteration.py`.
