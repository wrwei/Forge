# SRanger Convergence Trajectory — run-1

- **Study:** `sranger` (package dir `sranger`)
- **Actor:** `me-as-developer` (the driving Claude Code session edits Java
  directly; no `Agent`-tool sub-agents were dispatched)
- **Condition:** **B** (honest independent — `docs/CONVERGENCE_PLAYBOOK.md`
  and all answer-bearing material absent / not read)
- **Outcome:** **converged at iter 2** (cap 7)
- **Base commit:** severed single-commit base ("isolated run base") — no
  prior-run ancestry; see Independence below.

## Independence

Iter-1 cold codegen read **only** the canonical inputs allowed by
`experiments/RUN_TRAJECTORY.md §1`: the SRanger
`system_description.txt`, `requirement_all.json`, `CLAUDE.md`, the three
`forge.assets/prompts/*` codegen guides, `HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
and `RUN_TRAJECTORY.md` itself. Iter-2 read only the live workspace source
plus iter-1's `forge.assets/corrections/post_*` feedback. To write *correct*
cold code I also consulted the **pipeline implementation** (the tool, not the
answer key): `java2robochart.etl`, `thy_generation_rule.egl`, the coverage /
csp-corrections runners, and the generated artefacts (`.dfy`, `.thy`) — all
permitted ("derive the fix from the actual pipeline sources under
`forge.transformations/`"). Nothing under `docs/`, no `experiments/convergence/`
run/iter dirs, no `reference-runs/`, no `.remember/`, and no agent auto-memory
were read. No sranger-specific prior-run content was observed in the injected
session context at start.

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isa_gen | **fdr4** | **dafny_verify** | **isabelle_verify** | vacuity | pipeline (s)† | converged |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | Cold codegen (8 files, 185 LoC) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ inconclusive | ❌ postcond | ❌ timeout (615 s) | ✅ | 26.0† | no |
| 2 | turnVel 2→1; gate timed transition on Tick; Final tick self-loop (201 LoC) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ✅ 5 ver., 0 err | ✅ 10 lemmas, 21 s | ✅ | 54.5 | **yes** |

† **Pipeline wall-clock discounts hung (timed-out) phases.** Iter-1's `isabelle_verify`
ran to its ~600 s `deadlock_free` proof-timeout ceiling without a verdict (a wedge, not
productive verification), so it is excluded: raw 641.2 s − 615.1 s = **26.0 s** productive.
Raw `pipeline_wall_clock_s`, the discounted phase, and `pipeline_wall_clock_productive_s`
are recorded in each `iter-N/summary.json`. Iter-2 had no hung phase (productive = raw).

Token cost (`codegen_cost`): per-iter token counts are not separately
measurable from inside the driving `me-as-developer` session (`/cost` reports
cumulative session totals only and is unavailable as a tool here); recorded as
`kind: "estimated"`, values `null`, in each iter's `summary.json`.

## Iter-by-iter narrative

### Iter 1 — cold codegen (9/12 pass)

Wrote the full SRanger tree from the spec following `CLAUDE.md` conventions:
`annotation/RoboChartType`, `mode/SRangerMode {Moving,Turning,Final}`,
`constants/SRangerConstants` (the four real constants), `event/InputEvent`
(sealed signals `Obstacle`/`Tick`/`EndTask`), `sensor/Sensor` (`distance()`
with a large no-reading default), `actuator/Actuator` (`move(lv,av)` storing
the last command), `controller/Clock` (package-private `now()`), and
`controller/SRangerController` (mode-nested `step()` with named predicates
`obstacleDetected`/`turnDurationElapsed`, `clockResetTime` set from
`cycleClock.now()`, tick self-loops on Moving/Turning, and a terminal `Final`).
`result_codegen.json` maps all 22 requirement ids.

Modelling decision made up front from the ETL source: `Move(lv, av)` has **two**
payloads, but this ETL maps a record-in-a-sealed-interface to a single-payload
event, so a 2-field `Move` event would crash CSP-gen. I therefore modelled the
Move output as the multi-arg `actuator.move(lv, av)` operation call (the ETL's
documented `LOperations` path) and mapped SR-DM4 to it. The clock-reset field
`this.clockResetTime = cycleClock.now()` is auto-promoted to a RoboChart
`clock`, and `now() - clockResetTime >= turnDuration` rewrites to
`since(clockResetTime) >= turnDuration`.

Real environment / verdicts (after fixing two machine-specific tool paths in
`pipeline.yaml` — Dafny `Dafny.exe` and the WSL Isabelle binary, both pinned to
a stale `willr` home; local-only edits, not committed — and re-running so all 12
phases produced real verdicts):

- **fdr4 — inconclusive (out-of-range).** Not a deadlock/divergence verdict:
  `moveCall.0.2` — the controller communicates `turnVel = 2` on the `moveCall`
  channel, but the mandated `real` type range is `{0,1}`, so `2` is invalid and
  every assertion is inconclusive.
- **dafny_verify — postcondition.** `transitionFromTurning`'s generated
  postcondition `now()-clockResetTime >= 2.0 ==> mode == Moving` is unprovable
  because the higher-priority `endTask` branch sets `mode := Final` even when the
  guard holds.
- **isabelle_verify — timeout (615 s).** `deadlock_free` for the machine reduces
  to `⋀st. st = X ∨ …` closed by `by (metis St.exhaust_disc)`, which needs a bare
  `st = X` disjunct (a bare-precondition operation) for **every** state. `Final`
  had **no** outgoing transition → no enabled op → no `st = Final` disjunct →
  `metis` searched until the 10-min timeout. (The generated `.thy` confirmed
  `enumtype St = Moving | Turning | Final | initial` with `Final` a *regular*
  enum constructor — not a RoboChart `Final` type — so the fix is to give it an
  enabled op, not to special-case the name.)

### Iter 2 — three minimal feedback-driven fixes → converged

1. **fdr4:** `TURN_VEL` 2.0 → 1.0, so the communicated angular velocity fits the
   `{0,1}` range. The magnitude is irrelevant to the verified properties and the
   three Move commands stay distinct: `Move(1,0)`, `Move(0,1)`, `Move(0,0)`.
2. **dafny_verify:** gated the timed `Turning → Moving` transition on the Tick
   event — `else if (event instanceof Tick && turnDurationElapsed)` — so the
   generated postcondition's premise is event-specific and the `endTask` branch
   no longer falsifies it. The bare `tick` self-loop on Turning remains.
3. **isabelle_verify:** added a bare `Tick` self-loop on `Final`
   (`if (event instanceof Tick) currentMode = Final;`), giving `Final` an enabled
   bare-precondition operation exactly like the working Moving/Turning self-loops.
   `deadlock_free` then closed in 21 s (10 lemmas).

Result: all 12 phases pass, vacuity audit 0 findings. **Converged.**

## Caveats (run-specific compromises)

- **C1 — `turnVel` magnitude abstracted 2.0 → 1.0.** Forced by the mandated
  `{0,1}` FDR4 type ranges (RUN_TRAJECTORY §2): a velocity of 2 cannot be
  communicated on the Move channel. Verification-irrelevant (deadlock/divergence/
  contracts/invariants don't depend on the magnitude) and the three Move commands
  remain distinguishable, but it is a deviation from the spec's stated default.
  `turnDuration` (also 2.0) is **not** changed — it appears only in a guard
  comparison (`since(c) >= turnDuration`), never on a channel, so it raises no
  range error. Note its consequence: under `{0,1}` clock abstraction `since(c) ≤ 1`,
  so `since(c) >= 2` is never satisfiable in the abstract model — the timed
  Turning→Moving transition is present and contract-checked but not *exercised* in
  FDR4/Isabelle. (Reducing `turnDuration` to 1 would make it live but is an extra
  spec deviation and not needed to converge.)
- **C2 — Move output modelled as an operation call, not a 2-payload event.**
  SR-DM4 names a single `Move(lv, av)` output *event*; this ETL cannot represent
  a 2-payload event (records in a sealed interface become single-payload events),
  so Move is the multi-arg `actuator.move(lv, av)` operation (`LOperations`). The
  observable command semantics are preserved; the formal-model surface differs.
- **C3 — timed transition gated on Tick.** SR-Beh5 specifies an *autonomous*
  Turning→Moving; it is realised as a Tick-gated transition. Since Tick is
  delivered every control cycle (SR-Beh7), the timed transition still fires on the
  first tick after the duration elapses; the change is required for the Dafny
  contract to hold under the else-if priority the ETL flattens away.
- **C4 — `Final` is an absorbing live state, not a hard terminal.** It issues
  `Move(0,0)` on entry and then only tick-self-loops (never leaving). This keeps
  the robot halted while satisfying deadlock-freedom; it is not a RoboChart
  terminal/`Final`-typed node.
- **Local tooling, not committed:** `pipeline.yaml` FDR4 `timeout` 600→3600 and
  `memory_limit_mb` 4096→88659 (page-file MB) per RUN_TRAJECTORY §2.3; Dafny and
  WSL-Isabelle paths corrected from the stale `willr` home to this machine.

## Findings (durable, generalisable)

- **F1 — A `{0,1}`-range run cannot communicate any constant > 1 on a channel;
  the symptom is FDR4 "inconclusive", not a deadlock/divergence verdict.** The
  out-of-range value surfaces as `<channel>.….N is not a member of {0,1}` and
  makes *every* assertion inconclusive (0 passed) — easy to misread as a state-
  space/timeout problem. *Why:* the type-range correction pins channel value sets
  to `{0,1}` but constants keep their (ceiled) source value, so a payload of 2 is
  simply not in the channel's set. *How to apply:* when FDR4 reports all-
  inconclusive, run `refines.exe --format framed_json` on the discovered
  `*_coreassertions.csp` and read the `errors` field — if it names a channel
  value out of `{0,1}`, the fix is to abstract the *communicated* constant down
  to ≤1 (magnitudes don't affect the checked properties), **not** to widen the
  range (RUN_TRAJECTORY §2 pins `{0,1}`). Constants used only in guard comparisons
  do not trigger this — only channel payloads do.

- **F2 — A bare Tick self-loop on the terminal state is the surgical fix for the
  `deadlock_free` "Final" hang, when the machine has no typed-payload domain.**
  CLAUDE.md warns that "adding an operation on Final makes `apply deadlock_free`
  fail"; that warning is scoped to the *payload-domain* closer
  (`using St.exhaust_disc by auto`, the chemical_detector case). For an all-signal
  controller (SRanger), the generated theory uses `by (metis St.exhaust_disc)`,
  whose residual goal needs a **bare** `st = X` disjunct for every state. The
  terminal mode hangs only because it has *no* enabled operation; a bare Tick
  self-loop (no extra guard) gives it the missing disjunct exactly like the other
  modes' self-loops, and `metis` closes in seconds. *How to apply:* before
  reaching for CLAUDE.md's heavier "remove Final / reroute the terminal
  transition" fix, check the generated `.thy` for the closer in use — if it is
  `by (metis St.exhaust_disc)` (i.e. no typed-payload domain set), prefer adding a
  bare-precondition self-loop to the terminal state; it keeps all three spec modes
  and the terminal semantics (absorbing, `Move(0,0)` on entry) intact.

- **F3 — Read the generated `.thy`/`.dfy` to diagnose; the enum-state vs
  RoboChart-`Final`-type distinction matters.** The ETL turns *every* mode-enum
  value into a regular `RoboChart!State` (a state merely *named* `Final` is not a
  RoboChart `Final` node), and the EGL's `Final` handling keys on `isTypeOf(Final)`,
  not the name. Reading `SRangerController_Beh.thy` (the operations list and the
  `deadlock_free` lemma + its FORK comment) and `SRangerController.dfy` (the exact
  `ensures` clause and method body) turned three opaque verifier failures into
  three one-line root causes, and told me the surgical fix would work *before*
  paying a 10-min Isabelle run. *How to apply:* when a verifier fails, open its
  generated artefact and read the actual obligation/operation set rather than
  reasoning only from the Java.

- **F4 — Gating a flattened autonomous transition on its cycle event fixes the
  Dafny "preempted postcondition".** When an else-if chain has a higher-priority
  event branch (e.g. `endTask`) above an autonomous guard branch, the ETL extracts
  the autonomous transition with its guard as a stand-alone postcondition
  (`guard ==> mode == target`) and loses the "not the higher-priority branch"
  context, so the event branch falsifies it. Gating the autonomous branch on the
  per-cycle Tick event (`event instanceof Tick && guard`) makes the generated
  premise event-specific and disjoint from the higher-priority event, discharging
  the contract. (This is the documented HOWTO §3.2 pattern; confirmed here.)

## Reproducibility

1. Stage iter-N source: copy `run-1/iter-2/java/` into
   `java.generated.project/src/main/java/sranger/` (and `iter-2/traces/result_codegen.json`
   into `java.generated.project/`). For iter-1, use `run-1/iter-1/`.
2. Confirm `pipeline.yaml` `agent.active_case_study: sranger`,
   `forge.dashboard/corrections/type_ranges.json` all `{0..1}`, and the FDR4
   timeout/memory + Dafny/WSL-Isabelle paths point at your machine.
3. Run `python experiments/scripts/run_experiment_iteration.py`; authoritative
   per-phase verdicts are in `forge.assets/corrections/post_<phase>.json`.
4. iter-2 converges (all 12 phases pass, vacuity 0 findings) in ~55 s wall-clock.


## Execution time (recovered post-hoc)

Recovered from the driving session's transcript JSONL (first→last message timestamp); the per-phase deterministic times are from each `iter-N/summary.json`. Cross-run table: [../../execution_times.md](../../execution_times.md).

- **End-to-end (session):** 88.7 min
- **Pipeline (Σ deterministic phases, raw):** 11.6 min — FDR4 0.0 min · Isabelle-verify 10.7 min · other 10 phases 0.8 min
- **Agent (end-to-end − pipeline):** 77.1 min — codegen + feedback diagnosis + this write-up + idle (not pure codegen)
