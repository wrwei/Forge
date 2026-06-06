# chemical_detector — run-5 convergence trajectory

- **Actor:** me-as-developer (interactive Claude Code session; no sub-agents)
- **Condition:** B — honest independent (playbook absent; scrubbed, history-severed worktree; clean `CLAUDE_CONFIG_DIR`)
- **Converged:** YES, at **iter-2** (cap 7)
- **Date:** 2026-06-05
- **Base:** single parentless commit `00be5d2` "isolated run base (scrubbed tree of main; history severed for run independence)"

## Headline table

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline wall-clock | converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------:|-----------|
| 1 | cold codegen (19 files, 613 LoC) | ✅ | ✅ | ❌ (5× rule4) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 9/9 | ✅ | ✅ 9 lemmas + DF | ✅ 0 | 59.3 s | no |
| 2 | +5 `@RoboChartType("real")` annotations (2 files, +5 LoC) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 9/9 | ✅ | ✅ 9 lemmas + DF | ✅ 0 | 39.1 s | **yes** |

FDR4 ran against the auto-discovered `GasAnalysisController_System_Module_coreassertions.csp` (multi-controller aggregate; 3 `:[deterministic]` assertions stripped per pipeline policy); both iters completed FDR4 in ≤ 2.6 s — far below the 60-min kill policy. Isabelle proved all 9 structural-invariant lemmas plus `GasAnalysisController_deadlock_free` (23 s cold, 5 s warm).

## Iter-by-iter narrative

### Iter 1 — cold codegen

Read only the canonical inputs (system description, `requirement_all.json`, CLAUDE.md, the three prompt files, HOWTO, RUN_TRAJECTORY) **plus the pipeline's own transformation sources** (`java2robochart.etl`, `robochart2rct.egl`, `forge.dashboard/web/{runners,csp_corrections,feedback/coverage}.py`) — the tool, not the answer key — to pin down the Java→RoboChart encoding contract before writing a line of Java. Key design decisions taken up front:

- **Two controllers** (`GasAnalysisController`, `MovementController`), each a single-method mode-nested if-else machine with its own event sealed interface. Inter-controller `turn`/`stop`/`resume` are emitted by GA via the constructor pattern (`signals.send(new MovementEvent.Turn(this.anl))`) and consumed by MV via traditional `instanceof` — the EGL pairs them by event name into the `Shared` interface and direct `cref → cref` module connections.
- **No `Final` mode in either machine** (CLAUDE.md rule for the theory-generated controller): GA's terminal transition reroutes `GasDetected → Reading` while still sending `stop`; MV's `Found` self-loops on `stop`/`turn`/`resume` (also necessary to avoid inter-controller sync blocking once GA no longer terminates).
- **Complementary p/¬p guards** on the autonomous-only modes (`Analysis`: `stsIsNoGas`/`!stsIsNoGas`; `GasDetected`: `insAtOrAboveThr`/`!insAtOrAboveThr`; `AvoidingAgain`: `makingProgress`/`!makingProgress`) — total guard cover with no self-loops.
- **Every typed trigger captures its payload into a matching state var as the branch's first action** (`this.gs = g.value()`, `this.a = t.value()`, `this.l = o.value()`) so the M2T trigger-rebind avoids the `var v : real` type clash on non-real payloads.
- `move(lv, a)` only in transition actions (→ `LOperations` call); `changeDirection` payload wrapped in single-field record `VehicleEvent.ChangeDirection(Loc)` for clean event typing; `pause(n)` → `wait`; `d0/d1 = vehicle.odometer()` → `Sensors` var; clock field `tEvade` reset via `clk.nowMs()` → `# tEvade` + `since(tEvade)` predicates.
- Spec entry actions duplicated onto every non-self incoming transition so the EGL's lifting heuristic synthesises the `entry` blocks.

The extracted `.rct` matched the spec state machines essentially 1:1 (7 GA transitions, 24 MV transitions, all entries lifted as intended). Result: **11/12 phases passed on the first verdict** — only `preflight` failed, with 5 `rule4_double_missing_real_annotation` errors (`ChemConstants.THR/LV/STUCK_DIST`, `goreq`'s two `double` parameters).

*Environment repair (not a code iteration):* the first pipeline invocation reported `dafny_verify` and `isabelle_verify` failures that were pure tooling artifacts — `pipeline.yaml` carried another machine's user paths (`C:\Users\willr\...`, `/home/willr/...`; this machine uses `Will`/`will`). Per HOWTO §0 ("bridge failing, not the proof failing"), the paths were fixed as machine-local edits and the **same** Java was re-run; the snapshot records the honest verdict.

### Iter 2 — preflight fix

Applied the five `fix_directive`s verbatim: `@RoboChartType("real")` on the three double constants and the two `goreq` parameters (plus two import lines). No other changes. All 12 phases passed, vacuity audit 0 findings → **converged**.

## Caveats (run-specific compromises / tooling workarounds)

1. **`pipeline.yaml` machine-local edits, not committed:** `fdr4.timeout: 3600`, `fdr4.memory_limit_mb: 78025` (page-file size, per runbook §2), `dafny_verify.dafny_path` → `C:\Users\Will\dafny\dafny\Dafny.exe`, `isabelle_verify.wsl_isabelle_bin` → `/home/will/isabelle/...`.
2. **Iter-1 pipeline executed twice** (identical Java): first execution had exe-not-found environment failures in `dafny_verify`/`isabelle_verify`; only the post-repair execution is snapshotted.
3. **Spec deviations** (all documented in `iter-*/feedback/post_codegen.md`): no Final modes (terminal transitions rerouted, terminating events still emitted); p/¬p guards instead of `sts == gasD` / `goreq(ins,thr)` literal forms; odometer modelled as a `Sensors` variable rather than an event (the generated action language has no input communication); invented constant values (`THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=0.0, OUT_PERIOD=1`); `Chem` as a 2-literal enum; nat-spec'd constants render as `real` in the `Constants` interface (harmless at `{0..1}` ranges).
4. **`forge.assets/corrections/csp_overrides.csp` is LRE-legacy** in the scrubbed tree and is inert for this study: the loader reads `forge.dashboard/corrections/` (which holds only `type_ranges.json`), and its block names don't match this study's generated blocks anyway.
5. `Found` re-entry on a repeated `stop`/`turn`/`resume` re-fires the lifted `flag` entry action in the model (Java `flag()` is idempotent); a consequence of the no-Final rerouting.

## Run-level Claude token total

Summed from the driving session's transcript JSONL (`2d0cc4e5-…jsonl`, 190 assistant `usage` blocks; `/cost` is not agent-accessible), captured at archive time just before this write-up:

- input tokens (uncached): **15,651**
- cache-creation input tokens: **1,130,566**
- cache-read input tokens: **37,770,403**
- output tokens: **387,935**

Whole-run figures (both iters + setup + archive); not splittable per iter. Per-iter `codegen_cost` fields are `null`/`estimated` per protocol.

## Findings (durable lessons for future runs, any study)

**F1 — Read the transformation sources before cold codegen; they are the encoding contract.** CLAUDE.md describes *what* the M2M emits but not the exact Java shapes it pattern-matches. Spending iter-1 prep reading `java2robochart.etl` + `robochart2rct.egl` (≈1 hour of context) surfaced every encoding rule below and produced a model that was spec-faithful on the first extraction — the run converged in 2 iters with zero model-level failures. The runbook explicitly allows this ("the tool, not the answer key"); treat it as a standing step of iter-1, not an optional extra.

**F2 — M2M function-signature inference is first-parameter-only.** A multi-arg Java method referenced from a controller guard or action (e.g. the spec's `goreq(a, b)`) is emitted as a 1-param RoboChart function — arity mismatch downstream. Keep multi-arg helpers out of guards/actions: inline binary comparisons (`ins >= thr`) in named predicates, and reserve method calls in guards for single-parameter functions (`analysis(gs)`, `intensity(gs)`, `location(gs)`).

**F3 — Never give a controller constructor-dependency class a bare record-typed method/constructor parameter.** Phase-5 sensor detection walks the (last-discovered) controller's ctor-dependency classes and promotes the *first record found in any method parameter* to "the sensor record", hijacking the `Sensors` interface and deleting that record's `datatype`. Type such parameters as the sealed *interface* (`changeDirection(VehicleEvent cmd)`, `send(MovementEvent e)`), and instantiate emission ports inline (`= new SignalPort()`) rather than via constructor params.

**F4 — Use the single-field-record constructor pattern for every typed event emission.** `recv.method(new Events.Evt(arg))` names the event from the record and types it from the record's field via `recordMetadata` — correct in both transition and lifted-entry contexts. A bare single-arg call (`vehicle.changeDirection(this.l)`) in an entry context types the event from the *trigger* context (empty in entries) and synthesises a junk `<evt>_Type_value_int` primitive type.

**F5 — Every typed trigger branch must capture the payload into a matching state var as its FIRST statement.** The M2T rewrites `trigger evt ? v ; sv = v` into `trigger evt ? sv`; without the capture, the trigger binds to the stm-local `var v : real` and the CSP generator rejects non-real payloads (Angle/Loc/Seq). This includes consume-and-ignore self-loops (e.g. `Found` on `turn`).

**F6 — Prefer p/¬p (same predicate, negated) over two-enum-literal guards for total cover.** `stsIsNoGas` / `!stsIsNoGas` makes the autonomous pair's exhaustiveness propositional, so the Isabelle deadlock-freedom disjunction closes from `St.exhaust_disc` alone — no dependence on the payload enum's exhaustion lemma. Semantically identical on a 2-literal enum; document the deviation.

**F7 — Spec "entry" actions = identical action duplicated on every non-self incoming transition.** The EGL synthesises `entry` only when *all* non-self, non-initial incoming transitions carry the byte-identical action sequence (self-loops are excluded from the check, but re-entry still fires the lifted entry). Top-of-mode statements are the other (authoritative) entry source — but note multi-arg calls at top-of-mode are NOT routed through the operation-call builder, so `move(lv,a)`-style entries must come via the duplication route, never top-of-mode.

**F8 — Distinguish environment verdicts from code verdicts before burning an iter.** Exit-127 / exe-not-found in `dafny_verify`/`isabelle_verify` (tool paths from another machine in `pipeline.yaml`) look like phase failures in the SUMMARY but are not feedback about the Java. Fix the paths and re-run the same iter; snapshot only the honest verdict. Check `dafny_path.win32` and `wsl_isabelle_bin` against the local user homes during §2 setup, not after the first failure.

**F9 — The no-Final rerouting composes with inter-controller liveness.** Once the emitting controller (GA) no longer terminates, the receiving controller's terminal mode (`Found`) must keep accepting *every* event the emitter can still produce (`stop`, `turn`, `resume` self-loops) or the synchronous inter-controller connections can block. The reroute is therefore a pair of changes, not one: emitter terminal → live state, AND receiver terminal mode → consume-and-ignore self-loops on all shared events.

## Independence

- **Iter-1 inputs read:** `forge.assets/case-studies/chemical_detector/system/system_description.txt`, `forge.assets/case-studies/chemical_detector/requirements/requirement_all.json`, `CLAUDE.md`, `forge.assets/prompts/{java_codegen_rules,chain_of_thought_codegen,few_shot_codegen}.txt`, `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, `experiments/RUN_TRAJECTORY.md`; plus pipeline tool sources under `forge.transformations/src/main/resources/transformations/` and `forge.dashboard/web/` (sanctioned tool reads), and the workspace artefacts my own iters produced (`output/robochart_controller.rct`, `.dfy`, `.thy`, `post_*` feedback).
- Also opened: `forge.assets/corrections/csp_overrides.csp` (committed LRE-legacy infrastructure in the scrubbed tree — checked to confirm it could not affect this study) and `forge.dashboard/corrections/type_ranges.json` (setup §2 verification).
- **Not read:** anything under `docs/`, `experiments/convergence/**` (other than writing this run's own snapshots), `reference-runs/`, `experiments/cold-baseline/`, the findings knowledgebase (absent from the worktree), `.remember/` (absent), agent memory (clean config dir; no `MEMORY.md` lines were surfaced).
- **Harness-injected context at session start contained no study-specific prior-run content**: git history is the single severed base commit; no SessionStart hook output; no memory recalls. Tool-source comments in the ETL/EGL/template do mention chemical_detector by name (the pipeline was hardened on this study in earlier development); that is content of the tool itself, present in any condition.
- One run per session; no `Agent`-tool sub-agents used.

## Reproducibility

1. Check out the run base, stage iter-N: `cp -r experiments/convergence/chemical_detector/run-5/iter-<N>/java/* java.generated.project/src/main/java/chemdetector/` and `cp experiments/convergence/chemical_detector/run-5/iter-<N>/traces/result_codegen.json java.generated.project/`.
2. Set machine-local tool paths in `pipeline.yaml` (`fdr4.timeout: 3600`, `memory_limit_mb` = local page-file MB, `dafny_verify.dafny_path`, `isabelle_verify.wsl_isabelle_bin`); confirm `agent.active_case_study: chemical_detector` and `forge.dashboard/corrections/type_ranges.json` all `{0..1}`.
3. `python experiments/scripts/run_experiment_iteration.py` — expect iter-1 to fail only `preflight` (5 rule4 errors) and iter-2 to pass all 12 phases.

## Execution time (recovered post-hoc)

Recovered from the driving session's transcript JSONL (first→last message timestamp); per-phase deterministic times from each `iter-N/summary.json`. Cross-run table: [../../execution_times.md](../../execution_times.md).

- **End-to-end (session):** 29.1 min
- **Pipeline (Σ deterministic phases):** 1.6 min — FDR4 0.0 min · Isabelle-verify 0.7 min · other 10 phases 0.9 min
- **Agent (end-to-end − pipeline):** 27.5 min — codegen + ~1 h-equivalent pipeline-source reading + diagnosis + this write-up + idle (not pure codegen)
