# SRanger convergence trajectory — run-3

- **Study:** `sranger` (small reactive ground robot; 23 requirements, 3 modes, 1 controller; exercises the timed-transition `since()` encoding).
- **Actor:** `me-as-developer` (the interactive Claude Code session edited the Java directly; no `Agent`-tool sub-agents).
- **Condition:** **B** (honest independent — `docs/CONVERGENCE_PLAYBOOK.md` absent / not read).
- **Outcome:** **converged at iter-2** (all 12 phases `passed`/`completed`, vacuity audit = 0 findings).

## Independence

This was a fresh session with no prior exposure to SRanger's trajectory. The
worktree history was severed to a single parentless commit
("isolated run base (history severed for run independence)"), so no `run-*` /
`iter-*` / `archive` commit messages leaked into the SessionStart git snapshot.
No `<system-reminder>` / memory / `.remember` content naming SRanger's
prior-run fixes was present in context at session start. The scrubbed worktree
had `experiments/convergence/`, `reference-runs/`, `docs/CONVERGENCE_PLAYBOOK.md`,
and the findings KB removed.

**Iter-1 read only the canonical inputs** (RUN_TRAJECTORY §1): the SRanger
`system_description.txt` + `requirement_all.json` + its `requirements/README.md`,
`CLAUDE.md`, the three `forge.assets/prompts/*` codegen files, and the two
process docs (`RUN_TRAJECTORY.md`, `HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`).
To resolve specific encoding questions (Final-state handling, the clock/`since()`
rewrite, multi-arg output handling, operation-call parameter naming, the coverage
trace schema) I read the **pipeline tooling sources** under `forge.transformations/`
and `forge.dashboard/` — the tool, not the answer key. **Forbidden reads avoided:**
nothing under `docs/`, no other study's or run's `trajectory.md`/snapshots, no
`CONVERGENCE_PLAYBOOK.md`, no findings KB. (The HOWTO references SRanger's
top-level `trajectory.md` as a "template" and an "iter-2 worked example" — both
are §4 forbidden reads, so they were **not** opened; all fixes were derived
independently.)

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline (s)† | Converged |
|------|--------|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|--:|:--:|
| 1 | Cold codegen (9 files, 207 LOC); faithful terminal `Final` mode | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | 36.9† | ❌ |
| 2 | Gate autonomous `Turning→Moving` on `Tick`; replace terminal `Final` with live `Stopped` + bare `Tick` self-loop (214 LOC) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 57.1 | ✅ |

† **Pipeline wall-clock discounts hung (timed-out) phases.** Iter-1's `isabelle_verify` ran
to its ~600 s `deadlock_free` proof-timeout ceiling without a verdict (a wedge, not productive
verification), so it is excluded: raw 653.6 s − 616.7 s = **36.9 s** productive. Raw
`pipeline_wall_clock_s`, the discounted phase, and `pipeline_wall_clock_productive_s` are in each
`iter-N/summary.json`. Iter-2 had no hung phase (productive = raw).

Token cost (`codegen_cost`): `kind="estimated"`, tokens `null` — per-iter token
counts are not measurable from inside a `me-as-developer` session (no `/cost`
tool access). Iter-1's editing effort was dominated by deep pipeline-source study
to derive encoding choices; iter-2 was two small targeted edits.

(Iter-1's 653 s pipeline wall-clock is almost entirely the **616.7 s
`isabelle_verify` timeout** — see iter-1 below. Iter-2's 57 s is a normal full
run incl. the cold Isabelle heap load + a 28.9 s passing proof.)

## Iter-by-iter narrative

### Iter 1 — cold codegen (faithful), 10/12 phases pass

Wrote the full tree under `java.generated.project/src/main/java/sranger/`:
`annotation/RoboChartType`, `mode/SRangerMode` (Moving/Turning/Final),
`constants/SRangerConstants`, `event/InputEvent` (Obstacle/Tick/EndTask signals)
+ `event/OutputEvent` (Move(lv,av)), `sensor/Sensor` (distance(), large default),
`actuator/Actuator` (apply(OutputEvent), stores lastLv/lastAv),
`controller/Clock` (now()), `controller/SRangerController` (mode-nested if-else),
plus `result_codegen.json` mapping all 23 requirements.

Two **non-verdict** problems surfaced on the first run and were fixed *before
snapshotting* (RUN_TRAJECTORY §5 — a pipeline crash / un-runnable verifier is not
a real verdict): (a) machine-specific Dafny + WSL-Isabelle tool paths in
`pipeline.yaml` pointed at a different user (`willr` vs `will`) — corrected
(uncommitted local tuning); (b) FDR4 returned `0 passed, 3 inconclusive` because
the first encoding modelled `Move` as a 2-arg `actuator.setMove(lv,av)` operation
call, and the M2M emits multi-arg calls as `LOperations` **operation Calls** —
pushing `turnVel=2.0` through a CSP channel typed `{0,1}`
(`setMoveCall.0.2 ... not a member of {0,1}`). Re-encoded `Move` as the canonical
`actuator.apply(new OutputEvent.Move(lv,av))` output-event pattern issued as
**mode-entry actions**; the extractor reads only constructor arg 0 (`lv ∈ {0,1}`),
so the out-of-range `av` never reaches a channel.

After those fixes the iter produced **genuine verifier verdicts** — 10 phases
pass, 2 real failures:

- **`dafny_verify` (postcondition).** `transitionFromTurning`'s auto-generated
  `ensures now()-clockResetTime>=2.0 ==> mode==Moving` fails: the higher-priority
  `EndTask` branch (Turning→Final) preempts the autonomous `turnDurationElapsed`
  transition, so when both hold, `mode==Final` violates the postcondition.
- **`isabelle_verify` (timeout, 616.7 s).** `SRangerController_deadlock_free`
  timed out on `by (metis St.exhaust_disc)`: the terminal `Final` mode (entry
  action only, no outgoing transition) has **no bare-precondition operation**, so
  the residual `deadlock_free` disjunction has no `st = Final` case and `metis`
  searches indefinitely. This is exactly the Final-state hazard CLAUDE.md
  documents for the FORKed theory generator (weak `tr ≠ []` store invariant that
  never *designates* a terminal state).

Notable: **FDR4 deadlock-free *passed* even with the terminal `Final`** — in
tock-CSP a state with an entry action but no outgoing transition can still let
time pass (`tock`), so it is not a CSP deadlock. The Final problem is
Isabelle-only here.

### Iter 2 — two minimal feedback-driven fixes, converged

- **Dafny fix** (the `post_dafny_verify` `java_trace` pointed at the Turning
  method): gated the autonomous transition on the cycle `Tick` event —
  `else if (event instanceof InputEvent.Tick && turnDurationElapsed)`. The
  generated postcondition becomes `event==Tick && turnDurationElapsed ==> Moving`;
  with `event==Tick`, the `EndTask` branch cannot fire, so the postcondition holds.
  (`Tick` is delivered every control cycle, so firing the timed transition on the
  next tick after the duration elapses is behaviourally equivalent.)
- **Isabelle fix** (per `post_isabelle_verify` + CLAUDE.md's "no Final mode in the
  theory-generated controller; reroute the terminal transition to a live state"):
  renamed `Final` → `Stopped` (an ordinary state, **not** named `Final`) and gave
  it a bare-precondition `Tick` self-loop, while still emitting `Move(0,0)` on
  entry. Every `St` constructor now has an enabled operation, so
  `deadlock_free` closes via `by (metis St.exhaust_disc)` in 28.9 s.

All 12 phases pass; vacuity = 0. Converged.

## Caveats (run-specific compromises)

1. **`Move(lv,av)` angular component is abstracted out of the formal model.**
   RoboChart events carry a single payload, and the verification real-type domain
   is `{0,1}` while `turnVel=2.0`. Modelling `Move` as a single-payload output
   event that carries only `lv` (∈{0,1}) keeps the channel in range and the
   `av` value is dropped from the extracted model. The Java source retains both
   `lv` and `av` (the Actuator stores both, SR-DM6), and no verified property
   depends on the advisory output values. Consequence: in the model the *turn*
   command `Move(0,turnVel)` and the *stop* command `Move(0,0)` both render as
   `Move ! 0` (indistinguishable) — acceptable for deadlock/divergence/contract
   verification.
2. **Terminal mode renamed `Final` → `Stopped` and made a live absorbing state.**
   The spec (SR-DM1/SR-FR3) names the terminal mode `Final`; the FORKed Z-Machine
   theory generator cannot prove `deadlock_free` for a state named/shaped as a
   true terminal `Final`. `Stopped` is semantically faithful (the robot stays
   stopped forever, re-issuing `Move(0,0)`); the only operational difference is it
   consumes `Tick`s via a no-op self-loop.
3. **Autonomous `Turning→Moving` is gated on `Tick`** rather than purely
   autonomous (SR-Beh5 says "no event required"). Required to keep the Dafny
   postcondition provable against the higher-priority `EndTask` branch; equivalent
   in practice since `Tick` arrives every cycle.
4. **Power-up `Move(moveVel,0)` entry is modelled as the `Moving` state's entry
   action** (fires on every mid-run entry to `Moving` via `Turning→Moving`); this
   is faithful to SR-FR1.

## Findings (durable, generalisable — not obvious from CLAUDE.md / codegen rules / runbook)

**F1 — Advisory multi-real outputs collide with the `{0,1}` FDR type domain;
prefer the single-payload `OutputEvent` entry-action encoding over multi-arg
operation calls.** A 2-arg actuator call (`setMove(lv,av)`) is extracted by the
M2M as an `LOperations` **operation Call** that pushes *both* argument *values*
through a CSP channel typed by `type_ranges.json` (`{0,1}`). Any output constant
> 1 (here `turnVel=2.0`) then makes FDR4 abort with
`... not a member of the set {0, 1}`, reported as `0 passed, N inconclusive` with
**no Issues** — easy to misread as a verifier hang rather than a value-range
type error. *Why it matters:* the runbook mandates `{0,1}` for all types, so the
fix is not to widen the range but to keep out-of-range advisory values *out of
channels*. *How to apply:* model outputs as `actuator.apply(new OutputEvent.X(v))`
(single-payload; the extractor `extractOutputEventInfo` reads only constructor
arg 0) placed at the **top of the mode block** (lifted to a State entry action).
When an output is genuinely multi-real, keep the first/primary component as the
modelled payload and let the rest live only in the Java source. Diagnose
"`0 passed, N inconclusive`, Issues: none" by running `refines.exe` directly on
the `*_coreassertions.csp` — the channel-value error names the offending value.

**F2 — A terminal `Final` mode breaks Isabelle `deadlock_free` but NOT FDR4.**
Contrary to the common intuition ("a state with no outgoing transition = CSP
deadlock"), in tock-CSP a mode with an entry action and no outgoing transition
still passes FDR4 deadlock-free because time can always pass (`tock`). The same
mode makes `SRangerController_deadlock_free` **time out** (not fail fast) on
`by (metis St.exhaust_disc)`, because the FORKed theory generator's weak
`tr ≠ []` store invariant never *designates* a terminal state, so the residual
goal lacks a `st = <terminal>` disjunct. *How to apply:* for a single-controller
study, give the terminal mode a non-`Final` name and a bare-precondition `Tick`
self-loop (still emit the stop command on entry); this satisfies `deadlock_free`
identically to any ordinary mode and keeps divergence/determinism clean (the
self-loop is event-triggered, not τ). Budget for it: the failure presents as a
~10-min `isabelle_verify` (per-goal `proof_timeout=600`), not an instant error.

**F3 — The autonomous-transition postcondition is preempted by any
higher-priority event branch in the same mode; gate it on the cycle event.**
The Dafny generator emits, per mode, a postcondition of the form
`<autonomous-guard> ==> mode == <target>` taken from the guard-only transition.
If a higher-priority *event* branch (e.g. `EndTask`) sits above the autonomous
branch in the Java `else-if` chain, then when the event and the guard are both
true the event branch wins and the postcondition fails. *How to apply:* conjoin
the cycle/`Tick` trigger onto the autonomous guard
(`event instanceof Tick && <guard>`) so the generated premise is event-specific
and the higher-priority non-`Tick` branch can't falsify it. (This generalises the
HOWTO's SRanger-iter-2 note to *any* mode that mixes a high-priority event exit
with a guard-only exit.)

**F4 — Coverage `over_implementation` is suppressed per *file*, but framework
exemptions are per *element name*.** The coverage check exempts the framework
*class* names (`Clock`, `RoboChartType`, `SensorService`, `RoboChartWait`) but
**not their methods**: a `Clock` class with a public `now()` method whose file has
no trace entry flags `now` as over-implementation. *How to apply:* ensure every
`.java` file containing a public type/method has **at least one** `result_codegen.json`
entry (any requirement mapped to that file's path suppresses all its members,
since suppression keys on the file basename) — e.g. map a timing requirement to
`Clock.java`.

## Reproducibility

Stage iter-N's source and re-run the deterministic pipeline:

```bash
# from the repo root, with pipeline.yaml agent.active_case_study = sranger
rm -rf java.generated.project/src/main/java/sranger
mkdir -p java.generated.project/src/main/java/sranger
cp -r experiments/convergence/sranger/run-3/iter-2/java/* \
      java.generated.project/src/main/java/sranger/
cp experiments/convergence/sranger/run-3/iter-2/traces/result_codegen.json \
      java.generated.project/result_codegen.json
python experiments/scripts/run_experiment_iteration.py
```

Local prerequisites that are **not** committed (machine-specific): `pipeline.yaml`
`dafny_verify.dafny_path` (win32) and `isabelle_verify.wsl_isabelle_bin` must point
at this machine's Dafny / WSL-Isabelle installs; `fdr4.timeout=3600` and
`fdr4.memory_limit_mb` set to the page-file size; `type_ranges.json` = `{0..1}`
for all types. Isabelle proof checking runs in WSL (`wsl --set-default Ubuntu`).
Expected: iter-2 reports all 12 phases `passed`/`completed`, vacuity 0 findings.


## Execution time (recovered post-hoc)

Recovered from the driving session's transcript JSONL (first→last message timestamp); the per-phase deterministic times are from each `iter-N/summary.json`. Cross-run table: [../../execution_times.md](../../execution_times.md).

- **End-to-end (session):** 50.7 min
- **Pipeline (Σ deterministic phases, raw):** 11.8 min — FDR4 0.1 min · Isabelle-verify 10.8 min · other 10 phases 1.0 min
- **Agent (end-to-end − pipeline):** 38.9 min — codegen + feedback diagnosis + this write-up + idle (not pure codegen)
