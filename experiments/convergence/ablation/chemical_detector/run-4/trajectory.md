# Compile-Only Ablation — chemical_detector, run-4

- **Condition:** `compile-only-ablation` (only the eight compile-and-extraction
  feedback files visible; `post_fdr4`, `post_dafny_verify`,
  `post_isabelle_verify`, `post_vacuity` withheld and never opened).
- **Actor:** me-as-developer (LLM agent in the user's session, no sub-agents).
- **Session independence:** fresh session; no prior chemical_detector
  trajectory content was in context (no auto-memory or `.remember` hook
  injected anything; the persistent-memory index for this project was empty).
  `RUN_TRAJECTORY.md` referenced by LAUNCH.md does not exist in this isolated
  base (history severed); the run was driven from LAUNCH.md plus the
  scripts/CLAUDE.md alone.

## Setup

- `pipeline.yaml` `agent.active_case_study: chemical_detector` (already set).
- FDR4 type ranges already `[0..1]` in `forge.dashboard/corrections/type_ranges.json`.
- FDR4 kill policy applied locally: `timeout: 3600`, `memory_limit_mb: 65536`
  (page file size). Not to be committed.
- Wiped and recreated `java.generated.project/src/main/java/chemdetector/`.

## iter-1 — cold codegen → all 8 visible phases green (STOP)

Cold codegen from the canonical inputs only
(`requirement_all.json`, `system/system_description.txt`, CLAUDE.md +
prompt files). Before writing code, read the *pipeline implementation*
(java2robochart.etl conventions header and extraction operations,
StructuralLinter, coverage feedback module) to honour the extraction
conventions — these are infra sources, not study-trajectory or verifier
material.

Produced 17 Java files in package `chemdetector`:

- Two step()-controllers with enum mode fields:
  `GasAnalysisController` (Reading/Analysis/NoGas/GasDetected/Concluded)
  and `MovementController` (Waiting/Going/Found/Avoiding/TryingAgain/
  AvoidingAgain/GettingOut). Named boolean predicates; pure two-level
  if-else; typed-trigger payload captures as first action; clock field
  `evadeStart` reset via `clock.nowMs()`; `pause(...)` timing calls;
  2-arg `move`/`changeDirection` so they extract as operation calls.
- `Concluded` live sink instead of a RoboChart Final state on the
  gas-analysis machine (CLAUDE.md no-Final rule), with a guard-free
  gas-triggered self-loop; `Found` self-loops on `stop`. Autonomous-only
  modes (Analysis, GasDetected, AvoidingAgain) given total guard covers
  (complementary predicate pairs / two-literal enum split).
- Dispatcher facade `ChemicalDetector` (no mode field, no `step` method
  name) wiring GA → `AnalysisRelay` → MV.
- `result_codegen.json` covering all 81 requirement IDs;
  `post_codegen.{md,json}` self-review with 7 flagged issues (invented
  constant values, primitive Chem/Intensity realisation, Concluded-not-Final,
  invented classifier defaults, index→Angle mapping, during-action
  realisation, liveness/relay/arity encodings).

Ran `run_experiment_iteration.py` (all 12 phases execute; verifier output
not read — progress observed only via phase-header lines and a grep
filter restricted to the eight visible phases' verdict lines). Then
`check_visible_phases.py`:

```
compile passed; coverage passed; preflight passed; t2m passed;
m2m passed; m2t passed; dafny_gen passed; isabelle_gen passed
→ STOP: all 8 visible phases green
```

**Stop rule fired at iter-1.** No visible-feedback fixes were ever needed,
so there are no feedback-driven iterations to narrate. The four
behavioural verifiers ran as part of the same iteration; their outcomes
were recorded into `iter-1/summary.json` by the runner and were extracted
only afterwards by `record_ablation_result.py` (step D.3), never read
during the loop.
