# SRanger convergence trajectory — run-2

- **Study:** `sranger` (3 modes, 1 controller, timed-transition encoding)
- **Package:** `java.generated.project/src/main/java/sranger/`
- **Actor:** `me-as-developer` (the driving Claude Code session edits Java directly; no `Agent`-tool sub-agents)
- **Condition:** **B** (honest independent — `docs/CONVERGENCE_PLAYBOOK.md` absent / unread)
- **Converged:** iter-2 (2 iterations)
- **Commit base:** parentless "isolated run base (history severed for run independence)"

## Independence statement

Iter-1 cold codegen read **only** the canonical inputs named in
`RUN_TRAJECTORY.md` §1: the case-study `system_description.txt` and
`requirement_all.json` (+ its `README.md` ID conventions), `CLAUDE.md`,
the three `forge.assets/prompts/*` codegen rule files,
`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and `RUN_TRAJECTORY.md`. To derive
the timed-transition / clock encoding I read the **pipeline sources** (the
tool, not the answer key): `java2robochart.etl`, the generated `.dfy`, and
the generated `.thy` — all permitted ("derive the fix from the actual
pipeline sources under `forge.transformations/`").

No forbidden §4 path was read: nothing under `docs/`, no
`experiments/convergence/**/run-*`, no `trajectory.md`, no playbook, no
`CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md`, no `.remember/`, no agent-memory
file. No study-specific prior-run content was observed in injected context
(the severed base commit exposes no `run-*`/`iter-*`/`archive` ancestry).

**One disclosure (not a forbidden read):** the allowed input
`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md` (§3.2 "Special cases") contains a
sranger-flavoured worked-example note — "gate the autonomous on `Tick` …
see sranger/trajectory.md iter 2". I independently diagnosed the same
Dafny postcondition preemption from the *generated* `.dfy`
(`transitionFromTurning` line 70) before recalling that note; it is
disclosed here for completeness since the HOWTO is part of the canonical
input set.

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline (s)† | Converged |
|------|--------|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|--:|:--:|
| 1 | Cold codegen: 9 files, 3 modes (Moving/Turning/**Final**), Clock-promoted `clockResetTime`, autonomous `Turning→Moving` | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | 28.8† | No |
| 2 | (a) `@RoboChartType("real")` on `DEFAULT_DISTANCE`; (b) Tick-gate `Turning→Moving`; (c) rename `Final→Stopped` + `tick` self-loop | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 88.7 | **Yes** |

† **Pipeline wall-clock discounts hung (timed-out) phases.** Iter-1's `isabelle_verify` ran
to its ~600 s `deadlock_free` proof-timeout ceiling without a verdict (a wedge, not productive
verification), so it is excluded: raw 642.0 s − 613.2 s = **28.8 s** productive. Raw
`pipeline_wall_clock_s`, the discounted phase, and `pipeline_wall_clock_productive_s` are in each
`iter-N/summary.json`. Iter-2 had no hung phase (productive = raw).

Per-iter Claude token cost (`codegen_cost`): **not separately measurable**
in interactive `me-as-developer` mode — recorded as `kind:"estimated"`,
counts `null` in each `summary.json` (no `/cost` delta was captured; the
session is the cost). Iter-1's 642 s pipeline is dominated by the 613 s
Isabelle `deadlock_free` timeout; iter-2's 88.7 s reflects a clean run
(Isabelle proof closes in 28.5 s).

## Iter-by-iter narrative

### Iter 1 — cold codegen (3 failures, all genuine encoding mismatches)

Wrote the full source tree to `sranger/` following CLAUDE.md conventions:
`annotation/RoboChartType`, `mode/SRangerMode {Moving,Turning,Final}`,
`constants/SRangerConstants` (the four reals), `event/InputEvent`
(`Obstacle`/`Tick`/`EndTask` signal records) + `event/OutputEvent`
(`Move(lv,av)`), `sensor/Sensor` (`distance()` with a large default),
`actuator/Actuator` (recognised `apply(new OutputEvent.Move(...))` sink),
`time/Clock` (`nowMs()`), and `controller/SRangerController` — a
single-method mode-nested if-else machine. Entry actions sit at the top of
each mode block (ETL lifts them to RoboChart `entry`); the clock reset
`clockResetTime = timer.nowMs()` sits on the `Moving→Turning` branch (ETL
emits `# clockResetTime`); the timed guard
`timer.nowMs() - clockResetTime >= turnDuration` rewrites to
`since(clockResetTime) >= turnDuration`. 9 of 12 phases passed.

Three failures, each an encoding mismatch rather than a control-logic bug:

1. **preflight** — `Sensor.DEFAULT_DISTANCE` is a `double` without
   `@RoboChartType("real")`. The lint requires the annotation on **every**
   `double`, including a `private static final` sentinel that never
   surfaces in the model.
2. **dafny_verify** — `transitionFromTurning` postcondition
   `nowMs() - clockResetTime >= 2.0 ==> mode == Moving` could not be
   proved (`SRangerController.dfy:70`). The autonomous `Turning→Moving`
   guard becomes the postcondition premise, but the **higher-priority**
   `EndTask` branch preempts it: when `turnDurationElapsed` *and*
   `event == EndTask`, the controller goes to `Final`, not `Moving`,
   violating the ensures.
3. **isabelle_verify** — `SRangerController_deadlock_free` **timed out at
   613 s** (per-goal `proof_timeout=600`). The `Final` mode is absorbing
   (no outgoing `zoperation`), so the `deadlock_free` residual goal has no
   enabled-operation disjunct for `st = Final`; `by (metis St.exhaust_disc)`
   searches indefinitely. (A tooling note: the **first** iter-1 run failed
   `dafny_verify`/`isabelle_verify` with exit 127 — the committed
   `pipeline.yaml` had `willr` hard-coded in the Dafny win32 path and the
   WSL Isabelle path; corrected to this machine's `Will` / `/home/will`
   and re-ran with no Java change to obtain the valid verdicts above.)

### Iter 2 — three minimal feedback-driven fixes → converged

1. **preflight:** added `@RoboChartType("real")` to `DEFAULT_DISTANCE`.
2. **dafny_verify:** changed the autonomous branch from
   `else if (turnDurationElapsed)` to
   `else if (event instanceof InputEvent.Tick && turnDurationElapsed)`.
   The Dafny premise becomes `event == Tick && … ==> mode == Moving`, which
   `EndTask` (≠ `Tick`) cannot satisfy, so the postcondition holds. The
   companion `tick` self-loop (`else if event instanceof Tick`) now fires
   only when `!turnDurationElapsed`, giving `Tick` a total cover.
3. **isabelle_verify:** renamed the terminal mode `Final → Stopped` and
   added a `tick` self-loop (`Stopped --tick--> Stopped`). Now **every**
   state (initial/Moving/Turning/Stopped) owns a bare-precondition
   operation, so `apply deadlock_free; by (metis St.exhaust_disc)` closes
   in **28.5 s**.

All 12 phases pass, vacuity = 0 findings → converged.

## Caveats (run-specific compromises)

- **C1 — terminal mode renamed `Final → Stopped`, no longer strictly
  absorbing.** SR-DM1 names the third mode "Final"; SR-FR3/SR-Beh3/SR-Beh6
  make it a terminal stop. The forked Z-Machine theory generator emits the
  weak store invariant `tr ≠ []` (not `wf_rcstore tr st (Some final)`), so
  it never designates a terminal state, and a state named `Final` with no
  enabled operation hangs `deadlock_free` (iter-1). The mode is therefore
  modelled as an ordinary `Stopped` state with a single `tick` self-loop:
  it is a behavioural sink (no transition ever leaves it; it re-issues
  `Move(0,0)` every cycle), preserving the "robot is stopped for good"
  semantics, while being deadlock-free in the model. Mode **count** (3) and
  stop semantics are preserved; only the name and the strict-absorption
  property deviate.
- **C2 — autonomous `Turning→Moving` is now `tick`-triggered.** SR-Beh5
  specifies an autonomous (event-free) timed transition. To keep the Dafny
  postcondition provable under the `EndTask` priority, the transition is
  gated on `tick`. In a tick-driven system it fires on the first `tick`
  after `turnDurationElapsed` becomes true — behaviourally near-identical,
  but strictly it now consumes a `tick`. The RoboChart guard remains
  `since(clockResetTime) >= turnDuration`.
- **C3 — `Move(lv, av)` loses `av` in the model.** RoboChart events carry a
  single payload, so the recognised `actuator.apply(new OutputEvent.Move(lv,
  av))` sink extracts only the first constructor arg (`Move ! lv`). `Move`
  stays a faithful output **event** (per SR-DM4) at the cost of the angular
  component in the extracted model. (Modelling it as a 2-arg operation call
  would preserve both but demote it from an event — rejected as less
  faithful to SR-DM4.) No verification impact (deadlock/divergence/proofs
  are unaffected).
- **C4 — advisory M2M deadlock-lint warnings are expected.** `post_m2m`
  reports "State without unconditional fallback" for Moving/Turning/Stopped
  (status still `passed`). Each of those states *does* have a
  bare-precondition `tick` self-loop; the lint simply doesn't count an
  event-triggered self-loop as an "unconditional fallback". Per CLAUDE.md
  the lint is deliberately too broad; an `else { mode = Same; }` τ-self-loop
  would silence it but break FDR4 determinism, so it is correctly ignored.
- **Tooling (not committed):** `pipeline.yaml` Dafny path and
  `wsl_isabelle_bin` were repointed from `willr` to this machine
  (`C:\Users\Will\dafny\dafny\Dafny.exe`, `/home/will/isabelle/...`); FDR4
  `timeout` 600→3600 and `memory_limit_mb` 4096→88659 (page-file size) per
  RUN_TRAJECTORY §2. These are local tuning edits and are **not** committed.

## Findings (durable, generalisable)

- **F1 — A single-controller study with a required terminal mode forces a
  "live sink" rewrite, and renaming the mode away from `Final` is the
  mechanism.** CLAUDE.md says the theory-generated controller must have no
  `Final` state, but for a 1-controller study you cannot push `Final` onto
  a secondary controller. *Why:* a state named `Final` is treated by the
  pipeline as a terminal needing no operation, yet the forked generator
  never designates it terminal — so it hangs with no op, and (per CLAUDE.md)
  fails the reduction *with* an op. *How to apply:* rename the terminal
  mode to a non-`Final` name **and** give it one bare-precondition
  self-loop (here `tick`). This was decisive: same structure named `Final`
  hung at 613 s; named `Stopped` it proved in 28.5 s. The self-loop alone
  is not enough — the name must change too (a `Stopped` mode is structurally
  identical to `Moving`/`Turning`, which already prove with self-loops).
- **F2 — A diagnostic, not just a hang: the `Final` failure manifests as a
  per-goal `proof_timeout` on `*_deadlock_free`, not a fast tactic error.**
  `post_isabelle_verify` classified it `isabelle_tactic_timeout` on the
  `deadlock_free` lemma with `*** Timeout`. *How to apply:* if
  `isabelle_verify` *times out* (≈ the configured `proof_timeout`) on the
  `deadlock_free` lemma — as opposed to failing fast — suspect a state with
  no bare-precondition operation (an absorbing/`Final` mode) before
  touching any guard logic. Keep `proof_timeout` finite so the hang is
  bounded and reported rather than open-ended.
- **F3 — Gating an autonomous timed transition on the cycle `tick` is the
  fix when a higher-priority event preempts its Dafny postcondition.** The
  Dafny generator turns a transition's full guard (including any
  `event == X`) into the `ensures` premise. A bare autonomous guard yields
  `guard ==> target`, which any higher-priority event branch falsifies.
  *How to apply:* if `post_dafny_verify` flags a `dafny_postcondition` on a
  `transitionFrom<Mode>` whose only guarded outgoing edge is autonomous,
  add `event instanceof Tick &&` to that guard — the premise becomes
  event-specific and the operator/`EndTask` branches no longer violate it.
  Pair it with a plain `tick` self-loop so `tick` retains a total cover.
- **F4 — The preflight `rule4` annotation lint covers *every* `double`,
  including non-modelled `private static final` sentinels.** The iter-1
  block was a `DEFAULT_DISTANCE = 1000.0` constant that never reaches the
  RoboChart model. *How to apply:* in cold codegen, annotate **all**
  `double` fields/params/returns with `@RoboChartType("real")` up front
  (and `int`→`nat` where applicable), not only the ones you expect the M2M
  to lift — it saves a whole preflight iteration.
- **F5 — The clock encoding hinges on three exact Java shapes that the ETL
  pattern-matches; get them right in iter-1 and the timed transition "just
  works".** (1) a dependency field whose **declared type is `Clock`** (or
  `@Clock`); (2) a state field assigned `field = clock.nowMs()` (→ promoted
  to a RoboChart `clock`, reset emitted as `# field` on its transition);
  (3) the elapsed guard written **exactly** as
  `clock.nowMs() - field >= CONST` (→ rewritten to `since(field) >= CONST`).
  Detection is by the receiver's *type*, not the field/method name, so the
  Clock-holding field can be named `timer` (avoids the reserved word
  `clock`). This produced a correct `since(clockResetTime) >= turnduration`
  guard on the first try.

## Reproducibility

1. `pipeline.yaml` → `agent.active_case_study: sranger`; FDR4 type ranges
   `{0..1}` (`forge.dashboard/corrections/type_ranges.json`); repoint Dafny
   + `wsl_isabelle_bin` to your machine; FDR4 `timeout: 3600`,
   `memory_limit_mb:` = page-file MB.
2. Stage a given iter's source:
   `cp -r experiments/convergence/sranger/run-2/iter-<N>/java/*
   java.generated.project/src/main/java/sranger/` and copy that iter's
   `traces/result_codegen.json` to `java.generated.project/`.
3. `python experiments/scripts/run_experiment_iteration.py` — iter-1
   reproduces the preflight/dafny/isabelle failures (isabelle ~10 min
   timeout); iter-2 reproduces a clean 12/12 pass (~90 s).
4. Per-phase verdicts: `forge.assets/corrections/post_<phase>.json`
   `status`; timings: `phase_timings.json`. Each iter's snapshot under
   `run-2/iter-<N>/` carries the Java, feedback, traces, and formal
   artefacts (`.dfy`, `.rct`/CSP, `.thy`).

*KB note:* this run executed in a scrubbed worktree, so
`CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md` is absent here and was deliberately
**not** recreated; findings F1–F5 above are appended to it during copy-back
to the main checkout (RUN_TRAJECTORY §6 / Appendix).


## Execution time (recovered post-hoc)

Recovered from the driving session's transcript JSONL (first→last message timestamp); the per-phase deterministic times are from each `iter-N/summary.json`. Cross-run table: [../../execution_times.md](../../execution_times.md).

- **End-to-end (session):** 38.5 min
- **Pipeline (Σ deterministic phases, raw):** 12.2 min — FDR4 0.1 min · Isabelle-verify 10.7 min · other 10 phases 1.4 min
- **Agent (end-to-end − pipeline):** 26.3 min — codegen + feedback diagnosis + this write-up + idle (not pure codegen)
