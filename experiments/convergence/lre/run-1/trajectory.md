# LRE Convergence Trajectory — run-1

- **Actor:** me-as-developer (the interactive Claude Code session edited all Java directly; no sub-agents)
- **Condition:** B (honest independent — playbook absent)
- **Converged:** yes, at **iter 2** (cap 7)
- **Date:** 2026-06-05
- **Base commit:** `03d4606` ("isolated run base (history severed; answer material excluded from tree)")

## Independence

Iter-1 cold codegen read ONLY the canonical inputs: `forge.assets/case-studies/lre/system/system_description.txt`, `forge.assets/case-studies/lre/requirements/requirement_all.json`, `CLAUDE.md`, the three `forge.assets/prompts/*codegen*.txt` files, `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and `experiments/RUN_TRAJECTORY.md`. The session ran in a scrubbed worktree with a severed single-commit history and a clean `CLAUDE_CONFIG_DIR` (`C:\tmp\claude-clean-lre-run4`): no agent auto-memory, no `.remember` hook output, no study-specific prior-run content was injected at session start. Nothing under `docs/` was opened. Two pipeline *tool* sources were read for mechanics (permitted — the tool, not the answer key): `forge.dashboard/web/feedback/coverage.py` (to learn the `result_codegen.json` schema) and `forge.transformations/.../java2dafny.egl` (to learn the contract-emission rule when diagnosing iter-1's Dafny failures).

## Headline

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline wall-clock | converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|------------------|---------|--------------------:|-----------|
| 1 | cold codegen: 15 files, 667 LOC | ✅ | ✅ | ❌ (5 lint) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ❌ (3+ postconds) | ✅ 20 lemmas | ✅ 0 | 126.1 s | no |
| 2 | +5 `@RoboChartType("real")`; `tick` event + autonomous-guard exclusivity | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ✅ | ✅ 20 lemmas | ✅ 0 | 125.7 s | **yes** |

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs only, produced 15 Java files under `java.generated.project/src/main/java/lre/` (packages `annotation`, `mode`, `constants`, `event`, `sensor`, `actuator`, `operation`, `controller`):

- `LreMode` enum (OCM initial), `LreConstants` with the four spec-named thresholds (camelCase names kept so the extracted RoboChart constants match the requirement), sealed `InputEvent` (`reqVel`/`reqHdng`/`reqOCM`/`reqMOM`/`reqHCM`/`endTask`) and `OutputEvent` (`advVel`/`advHdng`) with deliberately lowercase record names matching RoboChart event names.
- `Obstacle` record + immutable `ObstacleRegister` (copy-on-write map, imperative loops only), `Sensor` with `hdist`/`vdist`/`odist`, field accessors, closest-static/dynamic selection (−1 sentinel), and the CPA geometry (`tcpaTo`/`cdaTo`, 3D incl. depth/rate-of-climb, divisor guard, time clamped ≥ 0).
- Five operation classes (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`) whose `compute()` bodies are pure `this.field = expression` assignments; the division-bearing CPA math lives in the Sensor layer because `compute()` may not declare locals or conditionals.
- `LreController`: mode-nested two-level if-else `step(InputEvent)`, eight controller state fields (Var1–8), operation invocations + field refresh at the top of step, named boolean predicates, all 18 behavioural transitions, entry actions inline (`advVel(1)` on MOM entry, `advVel(0)` on HCM entry, `advVel(0)` on Beh7/Beh18 exits).
- `result_codegen.json` tracing all 43 requirement ids.

**Result:** 10/12 phases passed immediately — including FDR4 (deadlock + divergence, 3/3 assertions), Isabelle (20 lemmas incl. deadlock-freedom; no `Final` state, bare-precondition cover via event-triggered branches per CLAUDE.md), and vacuity (0 findings). Two genuine failures:

1. **preflight** — 5 × `rule4_double_missing_real_annotation`: the four `Sensor.updateEnvironment` parameters and the private `SAFE_LARGE_DISTANCE` constant lacked `@RoboChartType("real")`.
2. **dafny_verify** — postcondition errors in `LreController.dfy` (lines 104/108/144): the generator emits `ensures <guard> ==> mode == <target>` for every guard-only transition with **no event premise**, so any higher-priority branch that fires while the guard holds (endTask/reqHCM in MOM; inOpez vs. the CAM/MOM guards in HCM) violates the contract.

(Environment note, not a verdict: see Caveat C1 — the first pipeline attempt had wrong machine-local tool paths; it was re-run with identical Java before snapshotting.)

### Iter 2 — minimal feedback-driven fixes

1. **Preflight:** added the five missing `@RoboChartType("real")` annotations in `Sensor.java`.
2. **Dafny:** read `java2dafny.egl` to confirm the emission rule, then (a) added an `InputEvent.tick` autonomous-evaluation event and gated all nine guard-only transitions (5 in MOM, 3 in HCM, 1 in CAM) on `event instanceof InputEvent.tick`, so each autonomous contract's premise is event-specific and operator events can no longer violate it; (b) made same-mode autonomous guards mutually exclusive by conjoining negations of higher-priority guards — only where targets differ (MOM: CAM-guard gets `!inOpez`; HCM-guards get `!inOpez && !collisionImminent`; the three MOM→HCM guards need no mutual negation since they share a target). New named predicate `collisionImminent = cda < minSafeDist && tcpa >= 0.0`, defined directly from fields/constants rather than from the other named booleans (defensive: contract emission inlines predicate locals; field-level definition avoids relying on recursive inlining). `tcpaNonNegative` removed (subsumed).

**Result:** all 12 phases passed; vacuity 0 findings. Converged.

## Caveats (run-specific compromises)

- **C1 — environment repair mid-iter-1 (disclosed):** `pipeline.yaml` shipped with another user's home paths (`dafny_path: C:\Users\willr\...`, `wsl_isabelle_bin: /home/willr/...`); the first pipeline attempt's dafny_verify/isabelle_verify never launched ("executable not found" / bash exit 127). Per RUN_TRAJECTORY §5 the attempt was not snapshotted; the paths were corrected (local tuning, not committed) and the pipeline re-run with byte-identical Java — the re-run's verdicts are iter-1's. During the broken first attempt I had briefly applied the preflight annotation fix; it was **reverted before the iter-1 re-run** so the snapshotted iter-1 reflects the true cold output, and the fix was re-applied as part of iter 2.
- **C2 — `tick` event is an implementation device** not present in the requirements. It models "autonomous evaluation cycle" as an explicit input. Tension with LRE-DC1 noted: several transitions share the (mode, tick) trigger, distinguished by mutually exclusive guards; each Beh requirement still maps to exactly one transition.
- **C3 — explicit priority negations** (`!inOpez`, `!collisionImminent`) were conjoined into lower-priority autonomous guards. Semantically identical to the Java else-if priority; encodes it explicitly so each transition's formal contract is self-contained (the fdr4_system.txt "workaround option 3" pattern, applied to the Dafny layer).
- **C4 — invented defaults:** `SAFE_LARGE_DISTANCE = 1000.0`; 3D CPA formulas (tcpa = −(r·v)/|v|², cda at clamped t ≥ 0); CAM has no entry action (spec names no evasive output and DM7 limits outputs to advVel/advHdng); Beh4's "1" thresholds taken as literal `1.0`.

## Run-level cost

- **Claude token total (whole run):** measured by summing the deduplicated per-message `usage` blocks of the driving session's transcript JSONL (`C:\tmp\claude-clean-lre-run4\projects\C--Users-Will-Gitee-fmgvc-lre-run4-redo\ec7c3db3-….jsonl`; `/cost` is not exposed to the agent). At write-up time: **~4.0 k fresh input + ~72 k output + ~280 k cache-write + ~6.40 M cache-read tokens** across 47 API messages (≈ 6.76 M total including cache reads). The archive-commit turns after this measurement add a small tail not captured.
- **End-to-end execution time:** session span (first→last transcript timestamp) ≈ **22 min** at write-up time (16:16:44 → 16:38:32 UTC; final figure incl. archiving ≈ 25–30 min).
  - **pipeline** = Σ snapshotted `pipeline_wall_clock_s` = 126.1 + 125.7 = **251.8 s (~4.2 min)**; the discarded env-broken first attempt spent a further ~51 s (counted in agent/other, not in pipeline).
  - **agent** = end-to-end − pipeline ≈ **18 min** at write-up time (codegen + diagnosis + write-up + idle).

## Findings (durable lessons for future runs)

- **F1 — Dafny autonomous-guard contracts are unconditional; gate autonomous transitions on a dedicated tick event AND make same-mode autonomous guards mutually exclusive.** The generator (`java2dafny.egl`, transition-contract section) emits `ensures <guard> ==> mode == <target>` for every guard-only branch, prepending `event == <E> &&` only when the branch checks an event. So *any* higher-priority branch — event-triggered or autonomous — that can fire while a lower-priority guard holds and that targets a different mode makes the lower contract unprovable (symptom: "postcondition could not be proved on this return path" pointing at the *preempting* branch's line). Two-part fix in the Java: gate every guard-only transition on a `tick` input event (kills event-branch preemption), and conjoin negations of higher-priority same-mode guards (kills autonomous-vs-autonomous preemption). Negations are needed **only between transitions with different targets** — same-target overlaps satisfy the contract trivially, so e.g. three →HCM guards need no mutual exclusion. Apply this shape from iter 1.
- **F2 — define compound named predicates from fields/constants, not from other named booleans.** The Dafny generator inlines step()'s predicate locals into contracts; a predicate defined in terms of another predicate relies on recursive inlining, which was not verified to work. Defining `collisionImminent` directly as `cda < minSafeDist && tcpa >= 0.0` was a zero-cost way to avoid the risk entirely (and is still legal predicate style).
- **F3 — preflight rule4 wants `@RoboChartType("real")` on *every* double, including ones that never reach the model.** Plain setter parameters (`Sensor.updateEnvironment`) and private static constants (`SAFE_LARGE_DISTANCE`) all tripped it. Cheapest policy: annotate every `double` field and parameter in every class at codegen time, not just the model-relevant ones.
- **F4 — verify `pipeline.yaml` machine-local tool paths during setup, before iter 1.** `dafny_path` (win32) and `wsl_isabelle_bin` are user-home-specific and the committed values may belong to a different machine/user. Symptoms are instant and unambiguous (dafny: "executable not found", 0.0 s; isabelle: bash exit 127 in ~4 s) but cost a full pipeline run + an Isabelle heap rebuild to discover mid-flight. A 5-second `Test-Path` / `wsl ls` check during §2 setup avoids burning an iteration attempt.
- **F5 — lowercase event record names work end-to-end and keep model names spec-faithful.** Naming the sealed-interface records `reqVel`/`advVel`/`tick` (not PascalCase) produced RoboChart events, CSP channels, and Dafny datatype constructors that match the requirement names verbatim, with no pipeline complaints anywhere (compile through Isabelle).

## Reproducibility

1. Check out the run-1 archive; copy `run-1/iter-N/java/` over `java.generated.project/src/main/java/lre/` and `run-1/iter-N/traces/result_codegen.json` to `java.generated.project/result_codegen.json`.
2. Ensure `pipeline.yaml` `agent.active_case_study: lre`, machine-correct `dafny_path`/`wsl_isabelle_bin`, fdr4 `timeout: 3600`, and `forge.dashboard/corrections/type_ranges.json` all `{0..1}`.
3. `python experiments/scripts/run_experiment_iteration.py` — iter-1 source reproduces the preflight + dafny_verify failures; iter-2 source reproduces full convergence (12/12 passed, vacuity 0).
