# LRE Convergence Trajectory — run-5 (condition B)

- **Actor:** `me-as-developer` (the session's LLM agent edited all Java directly; no sub-agents)
- **Condition:** B — honest independent (playbook absent; scrubbed worktree)
- **Study:** `lre`, package dir `lre`
- **Result:** **converged at iter 3** (all 12 phases pass, vacuity audit 0 findings)
- **Date:** 2026-06-05

## Independence

Iter-1 cold codegen read ONLY the canonical inputs: `forge.assets/case-studies/lre/system/system_description.txt`, `forge.assets/case-studies/lre/requirements/requirement_all.json`, `CLAUDE.md`, `forge.assets/prompts/java_codegen_rules.txt`, `chain_of_thought_codegen.txt`, `few_shot_codegen.txt`, `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and `experiments/RUN_TRAJECTORY.md`. Iters ≥ 2 read only the live workspace source plus the immediately-prior iter's `post_*` feedback and pipeline outputs (`LreController.dfy`). Nothing under `docs/` was opened; no `experiments/convergence/` content existed in the worktree (scrubbed). The only non-canonical repo file consulted was `forge.dashboard/web/feedback/coverage.py` (pipeline tooling, to learn the `result_codegen.json` schema). **No harness-injected contamination observed at session start:** no agent auto-memory entries, no `.remember` SessionStart hook output, no study-specific prior-run terms in any `<system-reminder>`. The worktree base was a single severed commit (`9bdb8724 isolated run base`), so git history leaked nothing.

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline wall-clock | Converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------:|-----------|
| 1 | Cold codegen: 15 files, 583 LOC | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ❌ 7 errors | ✅ | ✅ 0 | 97.7 s | no |
| 2 | `tick` event + autonomous gating + priority negations; `@RoboChartType("real")` on `SAFE_LARGE_DISTANCE` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ❌ 7 errors | ✅ | ✅ 0 | 98.4 s | no |
| 3 | LRE-Var1..8 quantities mirrored into controller fields; predicates read fields | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ✅ | ✅ 20 lemmas + deadlock-free | ✅ 0 | 104.7 s | **yes** |

FDR4 passed deadlock- and divergence-freedom on every iter (determinism stripped by `run_fdr4`, per pipeline policy). Isabelle passed on every iter once the WSL path was corrected (see Caveats).

## Iter-by-iter narrative

### Iter 1 — cold codegen

Generated the full `lre` package from the requirements: `annotation/RoboChartType`, `mode/LreMode` (OCM/MOM/HCM/CAM), `constants/LreConstants` (4 thresholds), `event/InputEvent` (sealed: `reqVel`, `reqHdng`, `reqOCM`, `reqMOM`, `reqHCM`, `endTask` — record names chosen to match the RoboChart event names exactly), `event/OutputEvent` (`advVel`, `advHdng`), `sensor/{Obstacle, ObstacleRegister, Sensor}` (safe-default returns for missing indices), `actuator/Actuator`, five `operation/` classes (CalcVel, CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA) invoked at the top of `step()`, and `controller/LreController` as the mode-nested if-else machine with named predicates. `result_codegen.json` traced all 52 requirement IDs.

**Outcome:** 10/12. `preflight` flagged one lint error (`Sensor.SAFE_LARGE_DISTANCE` double without `@RoboChartType("real")` — the rule covers constants, not just model-relevant fields). `dafny_verify`: 7 errors / 4 postcondition issues at `LreController.dfy` 95/99/135/138 — the generator emits each **autonomous** transition guard as an *unconditional* `ensures <guard> ==> mode == <target>`, which the higher-priority event-triggered branches (`endTask`, `reqHCM`) and higher-priority autonomous guards preempt.

### Iter 2 — tick gating + priority negations

Added a `tick()` input event (the periodic control cycle) and gated every autonomous transition on `event instanceof InputEvent.tick`, with explicit mutual-exclusion negations between **differently-targeted** guards only (`!inOpez`, `!camCondition` where `camCondition = cdaBelowMinSafeDist && tcpaNonNegative`); the three same-target MOM→HCM guards need no cross-negations. Annotated `SAFE_LARGE_DISTANCE`.

**Outcome:** 11/12; all four iter-1 Dafny issues *resolved*, but 7 errors remained — now at the tick branches themselves (.dfy 104/107/136/139). Root cause one layer deeper: guard predicates were inlined as calls to **uninterpreted functions declared `reads this`** (`inOpez()`, `cda()`, …). Any `mode := X` assignment has `modifies this`, so in the post-state Dafny cannot assume those function values are unchanged — every *other* transition's ensures premise might "become true" after the assignment, demanding a different target mode.

### Iter 3 — computed quantities as controller fields (converged)

Moved the eight LRE-Var1..8 quantities (`inOpez`, `hvel`, `vvel`, `vel`, `cstc`, `cdyn`, `cda`, `tcpa`) onto `LreController` as fields, assigned at the top of `step()` from the operation getters; the named predicates now read the fields. Dafny tracks fields precisely — `mode := X` provably leaves `this.inOpez` etc. untouched — so each ensures' field-based premise prefix is heap-stable and excludes every differently-targeted branch. Residual sensor-function calls in premises (`hdist(cstc)`, `vdist(cstc)`) are harmless: on every branch that assigns a different target the stable prefix is already false, and on fall-through paths nothing is modified, so post-state equals pre-state and the guard contradiction closes the proof. This is also the requirement-faithful reading of LRE-Var1..8 ("the controller **maintains** a … variable"). Traces for Var1..8 updated to the controller fields.

**Outcome:** 12/12, vacuity 0 findings. Converged.

## Run-level cost

- **Claude token total (whole run, source: session transcript JSONL `usage` blocks — `/cost` is not exposed to the agent):**
  - output tokens: **316.9 k**; uncached input: **22.8 k**; cache-creation input: **738.0 k**; cache-read input: **19.08 M** (140 usage blocks). Captured at archive time, just before the final commit; the commit turns themselves add marginally beyond these numbers.
- **End-to-end execution time** (session span, `max(timestamp) − min(timestamp)` over the transcript): **2105 s ≈ 35.1 min** (same capture moment as above).
  - **pipeline** = Σ `pipeline_wall_clock_s` over the three snapshotted iters = **300.9 s** (productive total; one additional ~95 s pipeline run was discarded as environmental — see Caveats).
  - **agent** = end-to-end − pipeline ≈ **1804 s ≈ 30 min** (codegen + diagnosis + write-up + idle; not pure codegen).

## Caveats (run-specific compromises)

1. **Machine-local tool paths were wrong and fixed mid-run (not an iteration).** `pipeline.yaml` shipped `dafny_path: C:\Users\willr\...` and `wsl_isabelle_bin: /home/willr/...`; this machine uses `Will`/`will`. Iter 1's first pipeline run failed `dafny_verify`/`isabelle_verify` purely environmentally; per RUN_TRAJECTORY §5 the run was discarded (not snapshotted), the two paths were fixed locally (uncommitted), and the pipeline re-run unchanged to obtain genuine iter-1 verdicts.
2. **`tick` is not in LRE-DM6.** The input-event set was extended with a control-cycle event to give autonomous transitions an event-specific premise. Semantically it models "one control cycle elapsed", not an operator command.
3. **Priority negations are redundant in Java.** The `!inOpez && !camCondition` conjuncts re-encode else-if priority explicitly; behaviour is unchanged.
4. **CAM entry action undefined in the spec** (LRE-FR4 says "evasive manoeuvres" but DM7 allows only advVel/advHdng) — transitions into CAM set the mode with no output.
5. **CPA math invented:** 2D horizontal CPA (`tcpa = -(r·v)/(v·v)`, `cda = |r + v·tcpa|` for `tcpa > 0`, else `|r|`); zero relative speed (incl. no dynamic obstacle) gives `tcpa = -1.0`, `cda = hdist(cdyn)` (safe large distance when no obstacle — reconciles DM5's zero-returning accessors with OP5's "large-distance defaults").
6. **`cstc`/`cdyn` annotated `nat` per LRE-Var5/6 wording** despite the -1 sentinel; no verifier objected (sentinel handling lives in the Sensor layer).
7. **Beh4's `odist > 1` guards use the literal 1.0** per the requirement text, not `minSafeDist`.

## Findings (durable, generalisable)

**F1 — The Dafny generator's ensures premise mirrors the Java guard *verbatim*, so every transition must carry its full firing condition in its own guard.** The generator translates each if-else branch to `ensures <branch-guard> ==> mode == <target>` with no added priority context: an autonomous branch `else if (inOpez)` becomes the unconditional `ensures inOpez() ==> mode == OCM`, which any higher-priority branch falsifies (iter-1 errors). *How to apply:* gate autonomous transitions on a cycle event (`tick`) so the premise carries `event == tick` (excluding all operator-event branches for free), and conjoin negations of higher-priority guards — but only against **differently-targeted** transitions; same-target overlaps prove trivially since every overlapping branch establishes the same conclusion.

**F2 — Guard atoms that survive into Dafny as uninterpreted `reads this` functions are heap-unstable: one `mode := X` assignment voids every other transition's postcondition.** This is invisible until the event-preemption layer (F1) is fixed, then surfaces as errors at exactly the branches that assign `mode` (iter-2). The Java-side fix is to **mirror each computed quantity into a controller field assigned at the top of `step()`** and write predicates over the fields: Dafny frames fields precisely, so premises built on fields survive mode assignments. A premise may still contain residual function calls (e.g. `hdist(cstc)`) provided its field-only prefix already excludes every branch with a different target — fall-through paths modify nothing, so pre/post coincide there and the guard contradiction discharges the obligation. Bonus: requirements phrased "the controller maintains a variable X" are pointing at exactly this design; following the spec wording literally *is* the verifier-friendly architecture.

**F3 — Verify machine-specific tool paths in `pipeline.yaml` (`dafny_path.win32`, `wsl_isabelle_bin`) BEFORE the first pipeline run.** They encode an absolute user-home path that silently breaks on a different machine/account (`willr` vs `will` here), costing a full pipeline run that must then be discarded as environmental. A 10-second `test -x` / `wsl ls` check during §2 setup avoids it. Symptoms: `dafny_verify` failing in 0.0 s with "executable not found", `isabelle_verify` exiting 127 with "No such file or directory" — never burn an iteration on these.

**F4 — Snapshot the iter BEFORE editing toward the next one.** The snapshot script copies the *live* workspace; mid-run I had edited iter-3's controller before snapshotting iter 2 and had to revert/re-apply to keep the iter-2 snapshot honest. Make "snapshot, then edit" a hard ordering rule in every iter loop.

**F5 — A minimal mutual-exclusion discipline keeps all four verifiers happy simultaneously.** Negating only the differently-targeted higher-priority guards (not building a full chain of all preceding negations) was enough for Dafny, kept the RoboChart guards short, did not disturb FDR4 deadlock/divergence-freedom, and left the Isabelle bare-precondition cover (event-triggered `reqOCM`/`endTask`/`reqVel`/`reqHdng` branches per mode) intact across all three iters — Isabelle's `deadlock_free` passed on every iteration without any dedicated work.

## Reproducibility

- Stage any iter's source: copy `run-5/iter-N/java/` over `java.generated.project/src/main/java/lre/` (wipe the package dir first) and `run-5/iter-N/traces/result_codegen.json` to `java.generated.project/result_codegen.json`.
- Set `pipeline.yaml` `agent.active_case_study: lre`, fix `dafny_path.win32` and `wsl_isabelle_bin` for your machine, FDR4 `timeout: 3600`.
- Run `python experiments/scripts/run_experiment_iteration.py`; authoritative statuses land in `forge.assets/corrections/post_<phase>.json`.
- Iter-3's source is the converged state: all 12 phases pass with vacuity 0.
