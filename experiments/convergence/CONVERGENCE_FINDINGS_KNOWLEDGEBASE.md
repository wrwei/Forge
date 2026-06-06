# Convergence-Experiment Findings — Aggregated Knowledge Base

**Purpose.** A single, deduplicated, provenance-attributed aggregation of every
durable *finding* and *caveat* surfaced across all FORGE convergence
trajectories for the three case studies (`lre`, `chemical_detector`,
`sranger`), mined from **all current trajectory write-ups *and* overwritten /
discarded / capped versions in git history** (e.g. the LRE "rebalance trap"
that lives only in iter-8 commit `26714c4`, the chemdetector "754k-process FDR4
wall" in `d2c0b83`). It also folds in the cross-run lessons from the three
per-study experiment write-ups and `experiment-protocol.md`.

> **Independence warning (read before any experiment).** This file is a
> *write-only findings sink*: runs **append** their durable findings here at
> archive time (`RUN_TRAJECTORY.md` §6) but MUST NOT **read** it during a run —
> in *any* condition (B, C, or D). It is listed as a forbidden read in
> `RUN_TRAJECTORY.md` §4. Condition-C knowledge augmentation uses
> `docs/CONVERGENCE_PLAYBOOK.md`, not this file. Provenance hashes below point at
> commits whose contents are themselves forbidden reads during a run.

**How to use.** §1 is experiment-level meta-findings. §2 is the
phase-keyed engineering knowledge (the part you consult while fixing a failing
phase). §3 is the cross-verifier "rebalance trap". §4 is operational/tooling.
§5 is one-time infrastructure/template bugs. §6 is the recurring spec-deviation
patterns. §7 is per-study profiles. §8 is the consolidated iter-1 checklist.

Provenance tags: study + run + short-hash, e.g. `(cd run-1 e6fff19)`,
`(lre iter-8 26714c4 — historical/capped)`. A finding tagged across multiple
studies is cross-validated.

---

## 1. Meta-findings (experiment-level)

- **M1 — Vacuity is non-negotiable; without the D1/I1 signals, "12/12 + 24
  lemmas" can be entirely vacuous.** Vacuity audit checks exactly two signals:
  **D1** = Dafny `ghost predicate Valid()` body is literally `true`/empty;
  **I1** = Isabelle zstore `where inv: "True"`. Either means the verifier
  proved nothing. The audit is itself weak — also spot-check generated `.dfy`/
  `.thy` after apparent convergence. *(lre-experiment; experiment-protocol)*

- **M2 — Convergence cost tracks *feedback specificity*, not requirement
  count.** Failures that surface at the *verifier* with a named transition /
  postcondition / state-space number are cheap (LRE: 4 iters, 81-ish reqs).
  Failures that originate *inside* the M2M/M2T chain surface as **moving
  downstream symptoms**; the LLM fixes locally and only pivots to the real
  (structural) cause after the same shape recurs 2–3 times (SRanger: 5 iters on
  only 23 reqs). A small study exercising a subtle pipeline-internal
  interaction is more iteration-expensive than a large study whose failures
  bottom out at the verifier. *(sranger-experiment)*

- **M3 — Iteration count is criterion-dependent.** Same trajectory "converges"
  at iter-1 if the bar is "compiles + structural phases", iter-2 if "FDR4
  alone", iter-3+ if "Dafny + Isabelle + FDR4 + vacuity". Always state the bar.
  *(lre-experiment)*

- **M4 — Cold (no-feedback) codegen converges 0/30.** K=10 per study: LRE
  9/10 compile but 10/10 fail Dafny cold; chemdetector **0/10 even compile**
  (multi-controller + typed payloads + shared-event routing); sranger 10/10
  compile but 10/10 Dafny-fail + 10/10 Isabelle-timeout (`clock` reserved
  word). The loop does real work. *(experiment-protocol)*

- **M5 — N=1 per study; counts are observations, not means.** LLM is
  non-deterministic; re-runs vary (see §7 per-study count spreads). Don't claim
  "study X converges in N iters" from one trajectory. *(experiment-protocol)*

- **M6 — Verifier semantics interact with system *shape*.** *Task-oriented*
  systems (chemdetector: find source, then `Final`) cannot claim whole-system
  deadlock-freedom — a true terminal compiles to `terminate → SKIP`, which is a
  deadlock under the failures-divergence model. *Continuous-control* systems
  (LRE: always running) don't hit this. The fix is the Final self-loop (§6).
  *(chemdetector-experiment)*

- **M7 — Strengthened obligations surface silent requirement ambiguities.**
  Newly-active Dafny postconditions forced an explicit "safety > operator"
  priority decision in LRE-Beh19 that iters 1–2 had silently mis-resolved. The
  loop's value includes *forcing articulation* of spec gaps — don't rewrite the
  spec, document the choice in caveats. *(lre-experiment; experiment-protocol
  "requirements are immutable")*

- **M8 — The loop also audits the *experimenter's* hypotheses.** Twice the LLM
  read the generated `.thy` and corrected a wrong human diagnosis (chemdetector
  iter-3: culprit was `thrval` emitted `unit ⇒ real`, not the hypothesised
  `intensityValue`). Diagnose from the generated artefact, not from a prior
  mental model. *(chemdetector-experiment)*

- **M9 — Headless one-shot agents thrash without cross-iter memory.** `claude
  -p --no-session-persistence` reapplied the same failed fix 3× (byte-identical
  `post_codegen.md`); only the thrashing-detector "change approach" escalation
  broke it. Drive real trajectories with a memory-carrying session (the
  me-as-developer mode), not the headless replay driver. *(sranger-experiment;
  HOWTO §2)*

---

## 2. Phase-keyed engineering findings

### 2a. Preflight (structural lint)
- **P1 — Annotate `@RoboChartType` on EVERY model-visible `double`/`int`, at
  fields, params, AND returns — including private fields and plain setters.**
  `rule4` fires on `private double` fields and on `Sensor.update(...)` /
  `goreq(a,b)` params that never reach an interface. Most-missed site: sensor
  `update`/setter parameters and record components. `double`→`"real"`,
  non-negative `int`→`"nat"`; never on locals or generic type args.
  *(lre run-1/run-2; cd run-2 65d39e0, run-3 d632eea; sranger)*

### 2b. FDR4 (the hardest gate — hard Windows resource ceiling)
- **B1 — Constants leak into the `Sensors` interface and blow up the state
  space.** The ETL treats a class as the `Constants` source only if its
  primitive fields are **all `public static final`** and ≥2. Instance
  `private final double`+getter compiles fine but leaks each constant into
  `Sensors` as a free `var` → each adds a ×2 dimension at `{0,1}` → page-file
  OOM (`VirtualAlloc MEM_COMMIT failed`). **Diagnostic:** `robochart_controller.rct`
  has an empty `interface Constants { }` and your thresholds appear as
  `interface Sensors` `var`s. *(lre run-1 7b06a22)*
- **B2 — Every `@RoboChartType`-annotated constant must be ≤ 1.** The harness
  pins all type ranges to `{0,1}`. A constant >1 *passed to a channel/operation
  arg or compared against a clock* becomes an out-of-range CSP literal →
  inconclusive/abort (`value … not a member of {0,1}`). Fractional ceil
  (0.5→1); integers >1 are NOT clamped (`turnVel=2`, `turnDuration=2` abort).
  Constants only used in a statically-unreachable comparison (`ins >= 50`) are
  safe. *(sranger run-3 50d7c4e; cd run-2 9a2740a; [[fdr-type-ranges]])*
- **B3 — Never store a `Seq`/`List` event payload in `Ctrl_State`.** A
  `List<X>` controller field becomes `LSeq(X,2)` ≈ 21 values/cycle at `{0,1}`,
  multiplying against every other var+mode. Three escape strategies, in order
  of fidelity:
  1. **Keep `Seq(GasSensor)` but rely on `{0,1}`** — finished in ~34 min, all
     9 assertions passed, *if* the payload only flows into uninterpreted
     functions and nothing else inflates `Ctrl_State`. *(cd run-3 d632eea/3b36180)*
  2. **Reduce to a single element** (`gas : GasSensor`) — payload is
     semantically dead in CSP anyway. *(cd run-1 e6fff19)*
  3. **Make `gas` a *signal* (no payload) + stage the reading on a Sensor
     cache** (`sensor.setLatestReading(...)`); functions read the cache. Drops
     the product space ~21×. Documented spec deviation from CD-Evt1. *(cd
     iter-4 b170a79 — historical)*
  Note the `LSeq` bound lives in the *official CSP generator*, not the project
  transformations — it is not Java-configurable. *(cd run-1 e6fff19)*
- **B4 — An unconditional `currentMode = X;` outside an event check is a
  τ-self-loop that FDR4 detects (divergence / `:[deterministic]`).** Gate it on
  `event instanceof InputEvent.Tick` (pattern-(b), event-only branch). This
  also supplies the bare precondition Isabelle needs (D2). *(cd d2c0b83/b170a79;
  playbook B4)*
- **B5 — FDR4 dominates wall-clock (~31–34 min for a multi-controller
  module); Isabelle is ~25 s once bare preconditions exist.** Budget iteration
  time around FDR4, not Isabelle. *(cd run-2 65d39e0)*
- **B6 — `:[deterministic]` failures from overlapping else-if guards are
  EXPECTED and stripped by the pipeline — never "fix" them.** They are inherent
  to RoboChart modelling priority chains as concurrent choices.
  *(fdr4_system.txt; all studies)*

### 2c. Dafny verification
- **C1 — A guard-only (autonomous) transition's auto-postcondition `guard ⟹
  target` is unprovable when a higher-priority event branch can preempt it.**
  **Preferred fix:** gate the autonomous transition on `Tick`
  (`event instanceof Tick && guard`) so non-Tick events make the premise
  vacuously true. **Fallback (no tick admissible):** order safety transitions
  first and conjoin explicit `!<higher-guard>` negations (a priority-negation
  cascade encoding the else-if priority the generator doesn't infer).
  *(lre run-2 a998d49; sranger; playbook C1)*
- **C2 — Tick-gating fixes operator-vs-autonomous preemption but NOT
  autonomous-vs-autonomous.** Two autonomous transitions leaving the same mode
  with overlapping guards and *different* targets stay mutually unprovable
  after gating — add the priority-negation cascade for those (transitions
  sharing a target need no exclusion). *(lre run-2 a998d49)*
- **C3 — A postcondition that reads an uninterpreted sensor function can't be
  framed after `mode := X`.** The generator emits sensor calls as `function …
  reads this`; under `modifies this` their post-state value is free. **Fix:**
  snapshot *only the specific quantities the postconditions reference* into
  controller fields once at the head of `step()` (not every predicate). ⚠️ Each
  snapshot field is a new `Ctrl_State` var → see §3 rebalance trap. *(lre run-2/
  run-3; sranger bead237 "snapshot pattern"; playbook C2)*
- **C4 — You can predict Dafny obligations from the Java before running.** The
  generator emits a postcondition for *exactly* the guarded transitions; a
  pure-event branch (instanceof, no extra guard) gets none (and is the
  deadlock-free bare precondition). Design the priority cascade up front; settle
  it with a fast `t2m→m2m→dafny_gen` + `Dafny.exe verify output/<Ctrl>.dfy`
  loop, not the full pipeline. *(lre run-2 a998d49)*
- **C5 — Put `compute()` calls + predicate/field assignments BEFORE the
  mode-dispatch if-else.** Both `findFirstCtIf` (Dafny) and the ETL treat the
  *first* `CtIf` in `step()` as the outer mode chain; an `instanceof`/guard `if`
  before it is mistaken for the mode chain → empty `interface Inputs {}`, no
  `InputEvent` datatype, "InputEvent not declared". *(lre run-4 a6bb040)*

### 2d. Isabelle / UTP Z-Machine
- **D1 — Every reachable `St` value needs at least one *syntactically bare*
  precondition op (`pre "st = X"`, no conjoined guard).** `deadlock_free`'s
  `metis St.exhaust_disc` / `using St.exhaust_disc by auto` only case-splits on
  the mode enum; a state whose every op is guarded (even a semantically total
  guard cover `P`/`¬P`) leaves a residual `st = X ∧ guard` the tactic can't
  close → 10-min timeout. **Diagnostic:** grep the `*_Beh.thy` for every
  `zoperation`'s `pre`, set-difference against the `St` enum; the state with no
  bare `pre "st = X"` is the culprit. **Fix forms:** (a) an event-only branch
  (`if (event instanceof Tick) …`); (b) for an autonomous binary split, make
  the last branch a plain `else` to a *different* state (bare, no τ-self-loop).
  *(cd run-2 65d39e0, run-3 d632eea; lre run-1; sranger run-3 50d7c4e)*
- **D2 — A terminal/`Final` mode is the classic D1 offender, and ONLY Isabelle
  catches it** (FDR4 models a final state as SKIP = not a CSP deadlock). The
  EGL template renders every Java mode as a regular `St` constructor and emits
  `inv: "tr ≠ []"` — it never emits the reference `wf_rcstore tr st (Some
  final)` `End` marker that `deadlock_free_z_machine` (`Z_Machine.thy:88`,
  `¬End ∧ Inv ⟶ dfp …`) uses to exempt terminals. **Fix:** give every terminal
  mode a visible-event self-loop (`if (event instanceof Tick) mode = Final;`) —
  event-triggered ⇒ no τ-divergence; no competing transition ⇒ no determinism
  cost. Do it in iter-1 to save a full ~35-min FDR4+Isabelle round-trip.
  *(cd run-3 d632eea, run-2 65d39e0, iter-4 b170a79; lre; sranger all runs)*
- **D3 — `Tick`, never `NoEvent`.** The Dafny generator auto-synthesises a
  `NoEvent` constructor; a hand-added `record NoEvent()` → `Duplicate datatype
  constructor name`. The HOWTO's `Tick` name exists to avoid this. *(lre run-1)*
- **D4 — A multi-statement event transition emits one duplicate `consts
  <SeqType>` per such transition → Isabelle rejects the theory.** Keep each
  event-triggered transition body single-statement; lift secondary statements
  to the *target* state's top-of-mode entry block (ETL attaches them as
  `EntryAction`s). *(cd d2c0b83/b170a79 — historical; playbook D3)*
- **D5 — A zero-arg sensor method used in a transition *action* (not guard)
  emits an undeclared function in the Isabelle update (`gs' = reading()` →
  "Extra variables on rhs").** The function-decl block only declares
  guard-referenced CallExps. Prefer an event payload or an arg-taking function
  for action-position reads on the Isabelle-verified machine. (Guard-position
  zero-arg reads are fine — they get the FORK `consts`.) *(cd run-3 556588b —
  historical)*
- **D6 — Reserved-word / auto-`consts` collisions break the `.rct` parse or the
  theory.** The EGL auto-emits `consts <name>` per guard CallExp; an Isabelle/
  RoboChart reserved word (`value`, `event`, `state`, `clock`, `transition`,
  `wait`, …) as an identifier won't parse. **Key distinction:** `step(InputEvent
  event)`'s parameter is SAFE (the controller is extracted as a state machine);
  sensor-method params, `Ctrl_State` field names, and single-field-record
  accessors (`.value()`) are UNSAFE — they reach `.rct`/`.thy` verbatim.
  *(chemdetector-experiment; playbook D1; java_codegen_rules "Reserved-word collisions")*
- **D7 — Only ONE state machine gets an Isabelle `.thy`.** In multi-controller
  studies (chemdetector) `isabelle_gen` emits a single theory (e.g.
  `GasAnalysisController_Beh.thy`); the other machine is covered by FDR4+Dafny
  only. Check the `.thy` machine name to know which you're proving; apply the
  bare-op discipline (D1/D2) to the unverified machine pre-emptively anyway.
  *(cd run-3 d632eea/556588b)*

### 2e. Data modelling (M2M extraction)
- **E1 — Don't wrap a single scalar in a record; use a primitive +
  `@RoboChartType`.** A `record Intensity(int)` / `record Chem(int)` is silently
  collapsed/mistyped: Dafny real/int mismatch, CSP guard typed `Int` not
  `Bool`, Isabelle `<T>_ext` coercion failure — all three point at the same
  wrapper. Reserve records for genuine multi-field aggregates. **Signal:** the
  same failure in all three verifiers at the same type. *(cd run-1 e6fff19)*
- **E2 — Model opaque/given types as the simplest concrete type the M2M maps
  cleanly:** totally-ordered scalar → `double` (real); equality-only identity →
  a small `enum`. Combined with `{0,1}` ranges this keeps payloads bounded.
  *(cd run-3 d632eea)*  (Tension with E1's "nat" advice across runs — E1's
  *anti-pattern* is the wrapper *record*; E2's enum/double on a plain field is
  the resolution. Both agree: no single-field record.)
- **E3 — The RoboChart function generator is single-parameter.** A 2-arg helper
  in a guard/action (`goreq(a,b)`) is emitted one-param → Dafny arity error +
  CSP return-type error + Isabelle operand error simultaneously. Inline binary
  comparisons in the guard (`ins >= thr`); keep the named function for coverage
  only (don't route the guard through it). Unary classifiers (`analysis(gs)`,
  `intensity(gs)`) are fine. *(cd run-1 e6fff19, run-2 65d39e0, run-3 d632eea)*
- **E4 — A multi-arg output (e.g. `Move(lv, av)`) — events carry ONE payload.**
  Three options: a record payload `Move(MoveCommand)` (cleanest, flows through);
  a multi-arg operation `Call` (M2M extracts state-var updates — works); or
  accept that the ETL takes only the first ctor arg and drops the rest
  (`Move ! lv`, `av` lost — fine if unverified). *(sranger run-2/3/4)*

---

## 3. The cross-verifier "rebalance trap" (the deepest finding)

Dafny and FDR4 pull in opposite directions through a *shared* Java construct:

- **Dafny** wants guard quantities **snapshotted into `Ctrl_State` fields**
  (C3) so frame reasoning sees them stable across `mode := X`.
- **FDR4** wants `Ctrl_State` **as narrow as possible** (B1/B3) — every field
  is a shared variable, ≈×2 state space at `{0,1}`.

The LRE iter-8 trajectory (`26714c4`, historical/capped) characterised this
empirically over 8 iters: hoisting **all 13** guard predicates took
`Ctrl_State` from 8→20 vars (~2¹³×) and OOM'd `refines.exe` at ~90k processes —
**reproducibly**, not a flake. iters with FDR4-fit failed Dafny; iters with
Dafny-fit OOM'd FDR4. Only narrowing the snapshot to the **6 scalar quantities
the postconditions actually reference** (count 10, one under the 11-var ceiling)
broke through. The chemdetector "754k-process wall" (`d2c0b83`, 11/12 capped)
is the same wall from the multi-controller `LSeq(GasSensor,2)` payload.

**Rules of thumb (playbook §E, validated by LRE iter-8):**
1. Types `{0,1}`, constants ≤ 1, `Ctrl_State` minimal.
2. Prefer **Tick-gating (C1)** over **field-capture (C3)** — Tick adds one
   event dimension; a captured real adds a full state var.
3. Never put a `Seq` payload in `Ctrl_State` (B3).
4. Capture only the *specific* terms Dafny can't frame over, not the whole
   sensor surface.

Single-controller studies fit comfortably with this discipline; multi-controller
(chemdetector) needs B3. **A controller already near the FDR4 commit ceiling
cannot apply the Dafny snapshot pattern without breaking FDR4** — that coupling
is a real, reproducible property the experiment surfaces, not a bug to wait out.
*(lre iter-8 26714c4; lre run-3/run-4; cd d2c0b83; playbook E)*

---

## 4. Operational / tooling findings

- **O1 — The CLI SUMMARY "completed" ≠ "passed".** It means the phase didn't
  throw. Authoritative status is `forge.assets/corrections/post_<phase>.json`
  `status` (and `snapshot_iter.py`'s `converged`). Multiple runs nearly declared
  convergence one iter early reading the CLI. Read the JSON. *(lre run-1/run-2;
  sranger run-1)*
- **O2 — Never invoke `wsl` (even a probe) while `isabelle_verify` is
  running.** A `wsl … ps` diagnostic during the dashboard's `wsl.exe` Isabelle
  bridge deadlocks the WSL VM *below* the pipeline's outer timeout → indefinite
  hang → full Windows reboot to recover. While a pipeline is in
  `isabelle_verify`, observe only the log file. To recover a genuine wedge:
  `TaskStop` the pipeline, then exactly one serial `wsl --shutdown`. Run only
  ONE Isabelle build at a time. *(lre run-1 7b06a22; HOWTO §0)*
- **O3 — Distinguish an FDR4 page-file OOM from a WSL/Isabelle hang by
  *reproducibility + which phase stalls*.** FDR4 OOM is a *model* signal
  (reproduces on standalone re-run; fix by shrinking `Ctrl_State`). A WSL hang
  is a *host* condition (log freezes mid-`isabelle_verify`; only
  `wsl --shutdown`/reboot clears). An FDR4 OOM that does NOT reproduce
  standalone is an environment flake — re-run `--phases fdr4` alone and
  re-snapshot `--force`. *(lre run-3/run-4)*
- **O4 — FDR4 memory caps don't bind the worker.** `pipeline.yaml`
  `memory_limit_mb` (`+RTS -M`) constrains `refines.exe` but NOT the
  `_refines.exe` worker, which grew to 41 GB and thrashed the page file long
  before the 600 s timeout. When FDR4 "hangs", check `_refines.exe` working
  set: tens of GB = model too large (reduce); flat working set + live process =
  normal CPU-bound normalisation (do NOT kill — interrupting yields an
  unreliable "pass"). *(cd run-1 e6fff19)*
- **O5 — Invoke `run_experiment_iteration.py` / `snapshot_iter.py` by ABSOLUTE
  path; drive with sequential tool calls; trust `post_*.json` over stdout.** The
  driver resolves repo root from `__file__`; a relative path silently fails if
  the shell cwd drifted. Large parallel tool batches desync output delivery.
  *(lre run-3/run-4)*
- **O6 — Machine-specific `pipeline.yaml` tool-path edits (`dafny_path`,
  `wsl_isabelle_bin`) are NOT committed** (RUN_TRAJECTORY §6). *(sranger run-3)*

---

## 5. One-time infrastructure / template bugs discovered (apply to ALL studies)

These were fixed in the pipeline (not Java) and benefit every future run; listed
so a future experimenter recognises them rather than re-diagnosing. *(mostly
chemdetector-experiment iter-9 + sranger run-4)*
1. **M2M boundary-STM emission (cd iter-9):** the canonical RoboChart pattern
   declares boundary events on the robotic *platform*, not `InputEnv`/
   `OutputEnv` stubs. The stubs' `initial→final` STMs translated to
   `terminate → SKIP` = a structural deadlock. Fix: emit boundary events on the
   platform, delete the env stubs, route connections directly.
2. **EGL clock-reset rendering (sranger iter-2):** SUBSECTION 4 emitted a
   malformed undeclared `clock=0` precondition for any `# <clock>` reset → now
   elided.
3. **EGL `since` const type (sranger iter-3):** `since` auto-declared
   `unit ⇒ real` but applied to a clock arg → now `'a ⇒ real`.
4. **Coverage `_FRAMEWORK_ELEMENT_NAMES` (cd):** originally only
   `RoboChartType`; CLAUDE.md defines four markers (`RoboChartType`, `Clock`,
   `SensorService`, `RoboChartWait`) — all four now exempt from over-impl.
5. **FDR4 csp-file auto-discovery (cd):** was hardcoded to LRE; now prefers
   `<Stm>Controller_coreassertions.csp` (no `_Module_` infix) for single-machine.
6. **Stale `trace_full.json` / `apply_csp_corrections` fallbacks (cd):** unlink
   on m2t failure; fallback for unset `csp_file`.
7. **`isabelle_verify` runner `NameError: 'finished'`** in the failure path (cd).

> **Protocol rule:** infrastructure fixes apply *symmetrically* — re-run every
> prior study's converged source against new infrastructure to confirm no
> regression. Never patch `expected_failures` to make a verifier pass
> (chemdetector iter-8 did this, reverted on methodological grounds, re-converged
> via the iter-9 boundary-STM fix). *(experiment-protocol)*

---

## 6. Recurring spec-deviation patterns (the standard "caveats")

Each is a documented, defensible deviation that recurs across studies — expect
to invoke them and record them in trajectory caveats.

- **SD1 — Invented `Tick` input event.** Not in any spec's event list; the
  per-cycle clock/heartbeat that (a) gates autonomous transitions for Dafny
  (C1), (b) gives terminal states a bare-precondition self-loop (D2), (c) avoids
  τ-divergence vs an autonomous self-loop. Lives in the already-traced
  `InputEvent.java` so coverage doesn't flag it. *(all studies)*
- **SD2 — `Final`/terminal self-loop on `Tick`** (D2/M6). Side-effect: the
  terminal idles on Tick rather than truly terminating. *(all studies)*
- **SD3 — Clock re-expressed without a RoboChart `clock`** (Isabelle theory gen
  can't reset a clock): as a bounded `nat` tick-counter (`turnTicks`), or a
  sensed `now()`/`nowMs()` reading compared in the guard, or hidden behind a
  non-`Clock`-typed Operation class so `containsClockCall` doesn't fire (the
  sranger iter-5 structural fix). Loses literal wall-clock semantics. *(sranger
  all runs)*
- **SD4 — Opaque/given domain types → primitives/enums** (E1/E2): `Intensity`→
  `double` (real), `Chem`→ small enum (or `nat`); the ordering predicate
  inlined as `>=` not a named function (E3). *(cd all runs)*
- **SD5 — An event consumed inside an entry/transition action → a sensor
  read.** `odometer ? d0` has no expression in the mode-nested `step()`; model
  as `d0 = vehicle.odometer()`. *(cd all runs)*
- **SD6 — Platform operations (`move`/`randomWalk`/`changeDirection`) as
  `Vehicle` methods called from actions, not `operation/` `compute()` classes**
  (they're actuators, not derived-value computations); timing `wait(n)` via a
  `@RoboChartWait`-annotated `pause(int)` (the Java name `wait` is reserved).
  *(cd run-3 d632eea)*
- **SD7 — Constants ceiled to `{0,1}`** for the FDR4 type-range flattening;
  spec defaults retained in Javadoc. Preserves control structure, abstracts
  magnitude. *(all studies)*
- **SD8 — Sensor layer returns safe defaults for the sentinel/no-data case**
  (e.g. `Double.MAX_VALUE`, not `0.0`) so controller predicates call sensor
  methods directly without `idx != -1` existence checks. **LRE bug:** returning
  `0.0` for `cdyn == -1` made `cda<minSafeDist` & `tcpa>=0` fire spuriously and
  *no verifier caught it* (the CPA formula is abstracted away in all three) —
  the only safety net was reading the spec (LRE-OP5). *(lre-experiment)*

---

## 7. Per-study profiles

### LRE (continuous-control AUV; 4 modes; ~continuous)
- **Iteration spread across runs:** 3, 2, 3 (current run-1/2/3); historical
  iter-8 capped at 11/12 (rebalance trap). Cold: 9/10 compile, 0/10 Dafny.
- **Signature traps:** constants leak → FDR4 OOM (B1); the rebalance trap (§3,
  the defining LRE finding); Tick-gating + priority-negation cascade for
  autonomous-vs-autonomous Dafny postconditions (C1/C2); `Double.MAX_VALUE`
  must not become a `static final` field; the CalcCPA sentinel bug (SD8).
- **Does NOT hit:** Final-state deadlock (no terminal mode), multi-controller
  payload explosion.

### chemical_detector (task-oriented mobile robot; TWO controllers — gas-analysis
+ movement; `Final` terminal)
- **Iteration spread:** current runs 6/2/2 (`6d65bfb`/`8a773ac`/`d632eea`);
  historical context-independent 4 (`3b36180`), early sub-agent run capped 9
  @ 11/12 (`d2c0b83`). **Cold: 0/10 even compile** — the only study that fails
  cold compilation (multi-controller + typed payloads + shared-event routing).
- **Signature traps:** `Seq(GasSensor)` payload explosion (B3, three escape
  strategies); single-field-record collapse (E1); 2-arg `goreq` in guard (E3);
  `Final` deadlock (D2 — *only* Isabelle catches it); only one controller gets
  a `.thy` (D7); boundary-STM `terminate→SKIP` deadlock (infra fix #1, M6);
  reserved-word collisions (D6); autonomous-only modes (`Analysis`/`NoGas`/
  `GasDetected`) where the "delete self-loop" recipe fails and Tick-injection or
  total-guard-cover-`else` is needed.
- **Cost:** ~2× LRE (iters + tokens); larger surface ⇒ trade-offs appear
  earlier and non-monotonically (iter regressed Isabelle while advancing FDR4).

### sranger (ground robot; timed turn; `Final` terminal; clocks — the ONLY study
with timing)
- **Iteration spread:** current 4/3/3; historical run-4 @ 5, manual @ 4,
  headless fresh-sample @ 7 (capped, functional @ 4). Cold: 10/10 compile,
  0/10 Dafny, 10/10 Isabelle-timeout (`clock`).
- **Signature traps:** the M2M `containsClockCall` classifier interaction (M2 —
  one root cause, three downstream symptoms across iters 2–4; resolved by
  Operation-class encapsulation iter-5); clock method must be `nowMs()` not
  `nowSeconds()`; the two EGL timing-template bugs (infra fixes #2/#3 — sranger
  is the first study to exercise the timed-transition→Isabelle path); `Tick` vs
  `NoEvent` (D3); the clock-since rewrite is independent of the Dafny event
  discriminator so one edit (`event instanceof Tick && nowMs()-tReset >= D`)
  satisfies both (run-3 50d7c4e).

---

## 8. Consolidated iter-1 cold-codegen checklist

Front-load these to compress the trajectory (most save a full ~35-min round-trip):
- [ ] Every `double` → `@RoboChartType("real")`, every nat-`int` →
  `@RoboChartType("nat")`, on **fields, params, AND returns** (incl. private
  fields, setters, record components). *(P1)*
- [ ] All domain constants `public static final`, values **≤ 1** for real/nat
  types reaching channels/clocks. *(B1, B2)*
- [ ] **No single-field wrapper records** — primitive + annotation, or a small
  enum for identities. *(E1, E2)*
- [ ] **No `Seq`/`List` event payload stored in a controller field** — keep it
  bounded under `{0,1}`, or cache on the Sensor + signal event. *(B3)*
- [ ] **No 2-arg function in a guard** — inline `>=`/`==`; keep named functions
  unary + for-coverage-only. *(E3)*
- [ ] Autonomous transitions gated on `Tick` (or explicit mutual-exclusion
  negations). *(C1, B4)*
- [ ] Every terminal/`Final` mode: `Tick`-gated self-loop, never an
  unconditional `mode = X`. *(D2)*
- [ ] Every non-final mode has ≥1 bare-precondition (event-only / `else`)
  outgoing branch. *(D1)*
- [ ] `step()` order: (1) `compute()` calls, (2) predicate/field assignments,
  (3) `if (currentMode == …)` chain — nothing branching before (3). *(C5)*
- [ ] Multi-statement event transitions: lift secondary statements to the target
  state's top-of-mode entry block. *(D4)*
- [ ] No reserved-word identifiers in UNSAFE positions (sensor params,
  `Ctrl_State` fields, record accessors): `value`, `event`, `state`, `clock`,
  `transition`, `wait`, … *(D6)*
- [ ] Use `Tick` (not `NoEvent`) for the autonomous/heartbeat event. *(D3)*
- [ ] `result_codegen.json` traces every requirement id to a Java element; every
  source file has ≥1 trace entry (file-basename suppression covers the rest).

---

## 9. Full-history consolidation (cross-branch pass, June 2026)

Mined across **all 13 branches and 446 commits**, not just the current branch.
Every entry was checked against §1–§8 and is *additional* or *sharper*, not a
restatement; IDs extend the existing series.

### 9.1 Quantitative results (the measured outcome)
- **Q1 — Convergence is a distribution, not a constant: 2–4 iters, median 3,
  and all three studies share the same median of 3** (LRE 3/2/3, chemdetector
  3/4/2, sranger 4/3/3 over 9 independent runs). Any single-run headline
  (including the earlier 4/9/5) overstates precision. *(cross-run-analysis;
  cf4e161)*
- **Q2 — Wall-clock is FDR4-dominated and bimodal by controller count.**
  Single-controller iters finish FDR4 in <1 min; the chemdetector two-controller
  parallel composition runs FDR4 ≈15–80 min/iter. Per-run totals: LRE ≈3–16 min,
  sranger ≈12–13 min, chemdetector ≈37–173 min; a non-converging Isabelle iter
  adds a ~10 min proof timeout. The cost rationale for the small-`Ctrl_State`
  discipline (§3). *(cross-run-analysis Finding 1b)*
- **Q3 — The chemdetector 9→~3 collapse measured pipeline *maturation*, not
  codegen difficulty.** The original 9-iter trajectory spent almost all effort
  on toolchain debt (reserved-keyword leaks, polymorphic `gas` channel, EventBus
  payload typing, the iter-9 EGL `InputEnv`/`OutputEnv` removal); once baked into
  the pipeline, fresh codegen converges in 2–4. Early counts conflate tool-build
  with codegen cost. *(cross-run-analysis Finding 5)*

### 9.2 Experiment methodology / protocol
- **M10 — Fix infrastructure once, commit it standalone, then restart the run;
  never edit a pipeline template (`*.egl`, M2M/M2T) mid-trajectory.** A
  template edit discovered mid-run *taints* the run (only Java/case-study
  artefacts may change within a trajectory) — the original sranger run-3 was
  discarded for exactly this and re-run clean. *(sranger run-3 re-run ef870de;
  RUN_TRAJECTORY §6; complements [[subagent-dispatch-banned]])*
- **M11 — Snapshot *before* editing the next iter.** Reconstructed snapshots
  reproduced faithfully, but provenance is fragile: chemdetector run-1/2 iter-2
  snapshots were rebuilt, LRE run-1 iter-1 went unsnapshotted (crash), LRE run-3
  collided into a run-4 slot. *(cross-run-analysis §4)*
- **M12 — Iteration count is a Pareto frontier over criterion strength.** The
  same LRE source "converges" at 1 iter (compile+structural), 2 (FDR4-only),
  4 (all-three+vacuity). State the bar *and* strengthen the M2T obligations as
  experiment **setup** — never mid-loop — or the count is undefined. *(lre-
  experiment as-executed addendum; extends M3)*
- **M13 — Convergence is non-monotonic across verifiers: a fix for one regresses
  another.** chemdetector iter-4 advanced FDR4 (flattened `GasSensor`) but broke
  Isabelle (accessor `intensityValue()` name-collided with a static helper →
  one wrong `consts`); an iter-2 Dafny framing helper introduced an Isabelle
  `unit ⇒ real` mismatch. Expect regressions; re-check all three each iter.
  *(chemical-detector-experiment iters 2,4)*
- **M14 — Single-case-study validation overstates genericity; budget ~6 pipeline
  fixes per *new* study.** The LRE-only round implied domain-independence;
  chemdetector then needed 6 infra fixes (§5) before a second study reached a
  measurable iter-1. Qualify RQ1 "domain-independent transformations"
  accordingly. *(chemical-detector-experiment)*
- **M15 — The loop only catches what the templates encode as obligations;
  operation-*internal* bugs escape all three verifiers.** The LRE CalcCPA
  sentinel bug (Sensor returned `0.0`, not `Double.MAX_VALUE`, for `cdyn==-1`,
  firing spurious CAM) survived 4 iters + 3/3 non-vacuous verification — `cda`/
  `tcpa` are uninterpreted in Dafny/Isabelle/FDR4, so no obligation links sensor
  defaults to outputs. Caught only by re-reading the spec + a property test.
  Relative-distance sentinels must default LARGE (see SD8). *(lre-experiment A4)*

### 9.3 FDR4 / determinism
- **B7 — B2 refined: an out-of-range constant is fatal only when it reaches a
  typed channel/operation arg** (`move(lv,a)` with `lv=2` → inconclusive); the
  same constant used purely in a comparison (`ins >= thr`, `thr=50`) is safe —
  that branch is just statically unreachable, which FDR4 tolerates. Tells you
  *which* large constants you can leave alone. *(CONVERGENCE_PLAYBOOK B2)*
- **B8 — FDR4/CSP-M reports one error per run; N CSP defects cost ~N iterations
  serially** (Dafny reports all per-file at once; Isabelle fails at the first
  goal but residuals are readable from the `.thy`). A single CSP defect *class*
  can consume several iters. *(chemical-detector-experiment iters 3-5)*
- **B9 — Use the UNTIMED CSP assertions for FDR4.** The tock-timed variant
  exhausts Windows virtual memory at identical official-RoboChart semantics.
  *(1fc65a5)*
- **B10 — On LRE the nondeterminism was NEVER else-if overlap — it was the
  τ-self-loop.** A controlled test re-introduced the four `else{mode=Same}`
  self-loops to converged source and exactly recovered the iter-1 deadlock+
  divergence+nondeterminism; no iteration's nondeterminism came from guard
  overlap. Implication: the M2M deadlock-lint is too coarse (doesn't distinguish
  event-triggered bare-precondition cover from guarded-only) and over-advises
  self-loops. *(lre-experiment "Caveat 2"; this is the controlled evidence
  behind [docs/fixes/pipeline_limitations.md](../../docs/fixes/pipeline_limitations.md))*

### 9.4 Dafny
- **C6 — Root cause behind C1, stated generally: a guard whose antecedent is
  built only from `reads this` functions under `modifies this`, with no stable
  (event-parameter) discriminator, is unprovable** — the post-state value is
  free on every sibling `mode := X` branch. Transitions already carrying
  `event instanceof X` verify for free. This *predicts* which obligations fail.
  *(sranger run-3 finding #1)*
- **C7 — EGL Dafny-typing fixes:** `inferArgDafnyType` must consult state-field
  and nested Spoon var-ref types or `int` fields (`cstc`, `cdyn`) emit as `real`
  in generated function params (da4dffc); non-boolean operation locals are
  emitted as abstract sensor functions to resolve unresolved Dafny identifiers
  (d874521). *(lre)*

### 9.5 Isabelle
- **D8 — Diagnose a `deadlock_free` timeout by set-differencing every
  `zoperation`'s `pre "st = X"` against the `St` enum**; the state with no bare
  `pre "st = X"` is the culprit (pinpoints `Final` in minutes instead of guessing
  at control flow). *(chemical_detector run-3)*
- **D9 — The generic `deadlock_free` tactic is selected structurally by a
  `hasPayloadDomain` flag.** Typed-payload-consuming initial states
  (`∃ gs ∈ SeqGs. …`) HANG `metis` unless the payload-domain set is emitted as
  `definition X :: "T set" where [simp]: "X = UNIV"` and closed with
  `using St.exhaust_disc by auto`; real-arithmetic-guard machines (LRE) keep
  `by (metis St.exhaust_disc)` because `auto` *times out* on them. The EGL now
  emits the `UNIV [simp]` set across CASES 0–5. *(170c78f;
  [[isabelle-heap-and-payload-domain]])*
- **D10 — Autonomous-only modes need a THIRD deadlock recipe: replace the
  τ-self-loop with a *visible* `Tick` self-loop** (`else if (e instanceof Tick)`).
  LRE's "delete the self-loop" recipe needs an event-triggered fallback the mode
  doesn't have; the total-guard-cover ideal isn't always available (chemdetector
  `Analysis`/`NoGas`/`GasDetected`). The Tick self-loop keeps Isabelle's bare
  `pre "st=X"` while removing the τ-edge that drove FDR4 divergence. *(chemical-
  detector-experiment iter-7; extends B4/D1 and the determinism trilemma)*
- **D11 — The Isabelle CallExp skip-set omits declared constants.** A constant
  in a guard (`thrVal`) is emitted as `consts thrVal :: "unit ⇒ real"` and
  clashes; Java-side workaround is to promote it to a `Ctrl_State` field (a
  zstore lens the template *does* filter); proper fix adds `declaredConstNames`
  to the skip set. *(chemical-detector-experiment iter-3)*
- **D12 — Tick-gating composes with the clock-`since` rewrite in ONE edit:**
  `event instanceof Tick && (clock.nowMs() - tReset >= D)` still emits
  `trigger tick` + `condition since(tReset) >= D` (the `nowMs()-field → since`
  rewrite is agnostic to surrounding conjuncts) while giving Dafny the `event`
  discriminator. So keeping a *real* timed model is compatible with Tick-gating;
  clock-elimination is not mandatory once the EGL timing bugs (§5 #2/#3) are
  fixed. *(sranger run-3 finding #2)*

### 9.6 Data modelling / reserved words
- **E5 — `Double.MAX_VALUE` / sentinel as a `static final` field is a double
  trap:** it trips preflight rule-4 (unannotated `double`) AND the Dafny
  generator lifts it to `const FAR: real := MAX_VALUE` (unresolved). Inline
  `Double.MAX_VALUE` at use sites; reserve `static final` for constants with a
  literal numeric RHS. *(lre run-3)*
- **E6 — A multi-value command models cleanly as an LOperations `Call`
  (`actuator.move(lv,a)`); a multi-field *event* payload silently drops all but
  the first ctor arg** (`OutputEvent.Move(lv,av)` → `Move ! lv`, `av` lost).
  Sharpens E4 with the silent-drop behaviour and the LOperations alternative.
  *(sranger run-3; 082674a)*
- **E7 — The M2M `containsClockCall` classifier lifts a *boolean-derived-from-
  clock* field to a `clock`.** `boolean turnDurationElapsed = clk.nowMs()-t >= d`
  was classified as `clock turnDurationElapsed`, so `condition turnDurationElapsed`
  was unresolvable; renaming field/package/initialiser all failed (it matches the
  clock *arithmetic in the RHS*). Fix: encapsulate timing in a non-`Clock`
  Operation class so the read is `turnTimer.elapsed()` on a non-Clock receiver.
  *(sranger-experiment iters 3-5)*
- **D6 sharpenings (reserved words / extraction surface):** the `clock` reserved
  word also leaks via the *package directory* name (`import sranger.clock.Clock`),
  not just field/method names; and `step(InputEvent event)` is "safe" only if
  `event` is never passed to an extracted helper — `currentOdometer(event)`
  lifted `event` into the `.rct` as `var event : nat`, so rename the call-site
  argument too. *(chemical-detector-experiment iters 1-3)*

### 9.7 Infrastructure / known issues
- **INFRA-8 — E6 branch-priority encoding was implemented then REVERTED.**
  `prependNegatedGuards`/`cloneExpression` (in `java2robochart.etl`) conjoined
  negated preceding guards to encode else-if priority, but the resulting
  guard-only transitions form τ-cycles under the tock-CSP maximal-progress
  `prioritise` wrapper — turning a *benign* `:[deterministic]` failure into a
  *genuine divergence*. The helpers remain defined but uncalled; re-enabling
  without inducing divergence is open work. *(docs/reconcile-e6-priority-status
  06e792c; docs/fixes/E6_branch_priority.md)*
- **INFRA-9 — Task-oriented top-level deadlock: re-diagnose the "structural
  ceiling" as a template bug; never statically suppress a *system-level*
  assertion.** chemdetector's `Final` termination failed module `:[deadlock
  free]`; iter-8 patched `expected_failures` (system module), then reverted on
  principle (suppress infrastructure artefacts only, never live system claims) —
  the real fix (iter-9) was the M2M boundary-STM change (§5 #1): the spurious
  `terminate → ✓` came from degenerate `initial→final` env stubs, not the
  system's shape. *(chemical-detector-experiment iters 8-9)*

### 9.8 Artefact ground truth (reference-runs/fix-readme-facts, b6ad69e)
Durable facts that contradict earlier docs — verify against these:
- chemical_detector ships **two** controllers (`GasAnalysisController`,
  `MovementController`), not three; there is no `ChemicalDetector` dispatcher
  class in the shipped `java/`.
- Dafny verified-method counts at convergence: **SRanger 5 / LRE 6 / Chem 7**
  (paper Table 2), not 4/8/8.
- The Isabelle Z-machine proof targets only `GasAnalysisController`; the movement
  controller is covered via the CSP/FDR4 path (the D7 consequence — its
  deadlock-freedom is FDR4-only, not Z-machine-proven).

---

## 10. Runs 4–6 consolidation (second independent wave, June 2026)

Synthesis of the second wave of independent condition-B runs (runs 4–6 × three
studies, all converged). Raw per-run findings are appended after the Provenance
index; the *fixable pipeline gaps* these runs exposed are tracked in
[`docs/fixes/pipeline_fidelity_gaps.md`](../../docs/fixes/pipeline_fidelity_gaps.md)
(items G-A…G-H). This section captures the cross-run **learnings**.

### 10.1 Convergence shifted down — for three different reasons
- Counts: LRE **2/2/2** (was 3/2/3), sranger **2/2/2** (was 4/3/3),
  chemical_detector **3/6/3** (was 3/4/2). Over 18 runs the per-study medians
  are now LRE 2, sranger 2, chemical_detector 3.
- **sranger = genuine pipeline maturation** (confirms Q3). The committed EGL
  timing fixes (`ef870de`) retired all three historical timing hazards
  (`containsClockCall` mis-lift, clock reserved-word leak, the two EGL timing
  template bugs) — none recurred. All three runs KEPT the real RoboChart clock
  (`since()` + Tick-gating) and it worked in iter-1. Timing is now a one-time,
  free cost — the dataset's clearest "infra fix ⇒ fewer iterations" evidence.
- **LRE = better cold codegen, NOT maturation.** The same template bugs still
  fired in iter-1 (record→`Seq` crash; function-only-in-arithmetic not declared).
  It dropped to 2 because iter-1 now gets the FDR4/Isabelle structure right and
  the actor applies the *complete* Dafny remedy (Tick-gate **and** cross-target
  negation) in one iter-2 batch. The **rebalance trap (§3) did NOT recur** —
  resolved by logic (gating/negation), not state-width.
- **chemical_detector = no improvement; run-5 is a 6-iter outlier (§10.2).**

### 10.2 The run-5 outlier: a `Final` state is *unprovable*, and it masquerades
- run-5's 6 iters were ONE root cause appearing as five problems: the GA
  `Final`-state `deadlock_free` proof times out, and two plausible
  CLAUDE.md-sanctioned fixes (boolean field; bare-precondition `else`) were
  misattributed before the real cause was found at iter-6.
- Root cause: the FORKed theory generator emits the weak invariant `tr ≠ []`
  instead of the reference `wf_rcstore tr st (Some final)`, so the residual goal
  has no `st = Final` disjunct and the closer searches forever. **Any `Final`
  state is genuinely unprovable** — this *corrects* the older D2 / CLAUDE.md
  claim that Final is handled by `St.exhaust_disc`. Fix: the theory-verified
  controller has no `Final` mode (reroute terminals to a live state). Now encoded
  in CLAUDE.md (`cf879f5f`) and tracked as gap G-G.
- Diagnostic: `apply deadlock_free; sorry` finishes in ~16 s → the *reduction*
  is cheap, the *closer* is the cost; the printed residual omits `st=Final`.
- Stacked on a real FDR4 explosion (`Seq(GasSensor)` → 64-min timeout at iter-3,
  fixed by reducing `Chem` to a single-value enum). So run-5 = two independent
  hard problems + two misattributed proof attempts.

### 10.3 `Final` / autonomous-mode discipline is closer-dependent (sharpens §9-D10)
- A **total guard cover** satisfies FDR4 (deadlock + divergence) but NOT Isabelle:
  `metis` *times out* on the residual `st=X ∧ guard`, and an `else{mode=same}`
  fallback is *vacuously false* under a total cover. Only an event-triggered
  (`Tick`) self-loop gives the bare precondition without τ-divergence.
- The apparent contradiction across runs (sranger run-4 kept `Final` + a Tick
  self-loop; cd run-5 had to REMOVE `Final`) resolves by the **proof closer**:
  *all-signal* machines (sranger, `by (metis St.exhaust_disc)`) tolerate a
  `Final` with a bare Tick self-loop; *payload-domain* machines (cd GA,
  `using St.exhaust_disc by auto`) cannot — `Final` must be removed.

### 10.4 New encoding gaps surfaced (all now in pipeline_fidelity_gaps.md)
Constants/functions/multi-arg op-calls in *state entry* actions are never
discovered (G-F); wait-duration constants used only inside `wait()` (G-E); a
function used *only nested in arithmetic* is never declared (LRE run-6);
record-with-`Seq`/`List` field crashes `isabelle_gen` — use a plain `final class`
(LRE run-5); the `>=` threshold guard has three dead ends, the only
four-verifier-clean form is a boolean field from a 1-arg boolean function (cd
run-5; G-A family); an uninterpreted function pinned to a constant makes a core
branch unreachable (G-C, highest impact); only the first controller gets
Isabelle+Dafny artefacts (G-D).

### 10.5 Methodology / independence (read before citing these runs)
- All nine are condition B in the new **severed-root scrubbed worktree** (strong
  *file* isolation). But every run self-flags that **`CLAUDE.md` (a sanctioned
  canonical input) carries study-specific durable guidance** (LRE-flavored
  phrasing; the GasAnalysis Final/determinism notes; the autonomous-mode
  trilemma). So these are **"condition B with standing CLAUDE.md guidance," not
  tabula rasa** — the fast iter-1→2 partly reflects pre-baked remedy knowledge,
  not pure cold rediscovery. This is the most important caveat for the paper's
  independence claim.
- Minor disclosed leaks: cd run-5's session git-status snapshot leaked run-4's
  iteration *count* (not its fixes; didn't shortcut — run-5 took 6>3); a stale
  root `result_codegen.json` survived the package wipe in cd run-4/6.
- **Tokens not recorded** in any run (all `codegen_cost` null) — the
  me-as-developer session can't read `/cost`; RUN_TRAJECTORY §3/§6 now record a
  run-level token total at archive instead. Wall-clock IS recorded, now with
  "productive vs raw" hung-phase discounting (`db2d5b0b`): Isabelle/FDR4
  *timeouts* are subtracted; long *passing* FDR4 (cd run-6 ~2.4 h) is not.

---

## Provenance index (commits mined; all are condition-B forbidden reads)

- **chemical_detector:** d632eea, 67b1063, 8a773ac, 65d39e0, e6fff19, 6d65bfb
  (current/recent); 556588b, 3b36180, 9a2740a, 0931142, b170a79, d2c0b83,
  8f55869 (historical/overwritten/capped).
- **lre:** 7b06a22, efb61b4, 5b67569, 26e4fe5 (run-1); a998d49, b66d76a (run-2);
  2c8d4b6, a6bb040 (run-3←run-4); 853de8d (discarded run-3); 7f35441, **26714c4
  (iter-8 rebalance-trap, historical)**, 10c8206, 5a7e325, 789beec, 95669dc.
- **sranger:** e5f98b5/e7c71ae (run-1), dce550e/df1fd28 (run-2),
  50d7c4e/83f4421 (run-3), 082674a (run-4); bead237, df6fd82, 8f235fa, 901de88.
- **Cross-branch pass (June 2026, §9):** 06e792c +
  `docs/fixes/E6_branch_priority.md` (E6 priority-encoding revert,
  branch `docs/reconcile-e6-priority-status`); 170c78f (generic `deadlock_free`
  tactic + `hasPayloadDomain`); ef870de (sranger EGL timing fixes committed);
  1fc65a5 (untimed-CSP for FDR4); da4dffc/d874521 (Dafny arg-typing);
  cf4e161 (CLI verdict fix); b6ad69e (reference-runs ground-truth facts,
  branch `reference-runs/fix-readme-facts`). Merged/empty branches checked:
  `requirements/strip-tier1-impl-detail`, `pipeline/no-determinism-check`
  (no unique findings), `experiments/anonymize-for-artifact` (path-anonymisation
  only).
- **Second wave (runs 4–6, §10; condition B, severed-root worktrees):**
  chemical_detector 77dfafc/300bbe98/86828022, sranger b1f1caaf/a8f9f23a/50c637eb,
  lre 81b025f6/2c4ad69e/3c5acafb; plus 64caaa91 (`docs/fixes/pipeline_fidelity_gaps.md`
  backlog G-A…G-H), cf879f5f (CLAUDE.md: theory-verified controller has no Final),
  db2d5b0b (productive-vs-raw wall-clock discount), e5e52ae1/ab12e80c (severed-root
  isolation), e67744c5 (forbid `docs/` reads during a run). Raw per-run findings
  are the appended blocks below.
- **Analysis docs:** docs/CONVERGENCE_PLAYBOOK.md;
  experiments/convergence/cross-run-analysis.md; the superseded first-round
  per-study + dated experiment write-ups, since removed — see git history for
  {lre-2026-05-21, chemical-detector-2026-05-22, sranger-2026-05-22}-experiment.md
  and the {lre, chemical-detector, sranger}.md summaries;
  experiments/experiment-protocol.md.

---

## chemical_detector — run-4 (condition B, base commit fcf51d5, 2026-06-02)

Converged at **iter-3** (3 iters; 12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent (playbook + §4 material scrubbed; agent-memory namespace empty, no `.remember` injection). Findings surfaced this run:

**F1 — Constants used *only* in `wait(...)` durations are not collected into the RoboChart `Constants` interface, so m2t fails CSP resolution.** The M2M constant-collection pass walks `transition.condition` and action assignments but NOT the `Wait.duration` slot; a wait-only constant renders as an unresolved `NamedExpression` (`Couldn't resolve reference to NamedExpression '<name>'`). Apply: pass `wait`/`pause`/`delay` durations as integer literals (`vehicle.pause(1)`), or also reference the constant in a guard/assignment so it gets collected. Keep it declared + requirement-traced for coverage.

**F2 — Enum/record event payloads only get their real type when the event is TRIGGER-typed or emitted with the OutputEvent-constructor pattern; a plain method-call send defaults the payload to a synthetic `int`.** `out.turn(anl)` (anl : enum) types `turn` as `turn_Type_value_int` and FDR4 rejects `turn.out!anl`. `markEventTyped` on the action side (no trigger) falls back to a synthetic `value_int`; only the trigger side and `extractOutputEventInfo` read the real payload type. Apply: emit every typed output/shared event as `actuator.apply(new <EventRecord>.<Sub>(payload))` with `<Sub>` a single-field record carrying the enum/record payload. Untyped signal events (zero-field records) are fine either way. Single most load-bearing convention for models with enum-carrying outputs.

**F3 — For a shared (inter-controller) event, the FIRST machine's event object wins the cross-machine type.** `robochart2rct.egl` keys the Inputs/Outputs/Shared declaration by the first `Event` EObject seen with that name across `pkg.machines`; a producer emitted first that types the event weakly (int) overrides a consumer that types it correctly. Apply: make the **producer** emit a correctly-typed event (F2) rather than relying on the consumer's trigger typing to win. F2+F3 together fully fix shared enum events regardless of machine order.

**F4 — Autonomous-only modes can be made deadlock-free for the Isabelle proof WITHOUT a τ-self-loop by routing the lowest-priority branch through an unconditional `else` to a *different* state.** A mode whose every spec'd transition is guard-only needs a bare `pre "st=M"` op, but an `else { mode = M; }` self-loop is a τ-divergence. `if (gasD) {→GasDetected} else {→NoGas; resume}` gives the `else` a bare precondition AND a non-self target → deadlock-free, divergence-free, only loses determinism (not checked). Sidesteps the CLAUDE.md autonomous-mode trilemma without a `Tick` event. (`Tick`-self-loops remain the tool for genuinely *terminal* states.)

**F5 — `dafny_verify` / `isabelle_verify` failing with exit 127 / "executable not found" is a TOOLING path problem, never a code problem.** Committed `pipeline.yaml` defaults bake in one author's username (`willr`); on any other machine both `dafny_verify.dafny_path` (win32) and `wsl_isabelle_bin` must be re-pointed. Apply: in §2 setup, before iter 1, verify both resolve (`ls` the Dafny exe; `wsl -- bash -lc 'ls <wsl_isabelle_bin>'`) and fix locally (uncommitted). Don't burn a code iteration diagnosing a 127.

**F6 — Two-controller FDR4 on `{0..1}` type ranges is fast (~4 min), not a 60-min risk.** The composed `chemical_detector` module checked 9 assertions in ~238 s. The 60-min FDR4 policy is a safety ceiling; with `{0..1}` ranges this model does not approach it.

---

## chemical_detector — run-5 (condition B, base commit 77dfafc, 2026-06-02)

Converged at **iter-6** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent. Disclosure: the run-5 worktree was based off `77dfafc` (the run-4 archive commit), so the session-start git-status snapshot leaked run-4's commit message — i.e. that run-4 converged in 3 iters. Iteration-count leakage only (no fixes); run-5 took 6 iters. **Process fix for future runs: base the worktree off a commit BEFORE the prior run's archive, so `git log`/status can't leak the prior run's iteration count.** Findings surfaced this run:

**F1 — Inter-controller events need no special handling; match by simple name.** An event that is an action in one machine and a trigger in another auto-classifies as `Shared`. Make the `OutputEvent.X` constructor in the sender's send and the `InputEvent.X` in the receiver's `instanceof` share the **same simple name** (`Turn`/`Stop`/`Resume`); the ETL/EGL unify them into one `<Diagram>_System_Module` with direct cref→cref connections.

**F2 — Multi-arg operation calls and constant-bearing entry actions must live on incoming transitions, not top-of-mode.** `attachTopOfModeEntryActions` does not call `buildOperationCall`, so a 2-arg `vehicle.move(lv,a)` at the head of a mode block becomes the broken event `move ! 1` (2nd arg dropped); and the EGL collects action-referenced constants only from **transition** actions, so a constant used only in a top-of-mode `wait(evadeTime)` is referenced-but-undeclared → CSP-gen "Couldn't resolve reference to NamedExpression". Put such entry actions on each *incoming transition* (identical across all of them); the common-incoming-action lift turns them back into a RoboChart `entry`, constants get collected, and `move(lv,a)` becomes a real `LOperations` call.

**F3 — Express a real-valued threshold guard as a boolean state field, never inline-`>=` nor a 2-arg function.** Dead ends for `ins ≥ thr`: (a) inlined `ins >= thr` forces Isabelle `deadlock_free` into real linear-arithmetic; (b) a 2-arg `goreq(ins,thr)` hits the single-parameter function emitters (CSP "wrong number of arguments", Dafny arity error) and, since a method-call predicate doesn't inline, emits the predicate as a `Sensors var : nat` in a Bool position → FDR4 "expected Bool, actual Int". The form that satisfies all four verifiers: a **boolean controller field** `aboveThr`, assigned in the source state's entry from a **1-arg** boolean function (`aboveThr = overThr(ins)`), guarded `aboveThr`/`!aboveThr`. (Bonus: a function-call assignment RHS renders in the theory `update`; a bare `>=` comparison RHS renders as an **empty** update RHS → Isabelle parse error.)

**F4 — A primary controller whose Isabelle theory is generated must NOT have a `Final` state.** Dominated this run (iters 1–5 all failed only on `deadlock_free`). The FORKed theory generator emits `where inv: "tr ≠ []"` instead of the reference's `wf_rcstore tr st (Some final)`; without the `(Some final)` designation, `deadlock_free` treats the terminal `Final` as a normal state needing an enabled op (it has none), so the residual disjunction lacks a `st = Final` disjunct and is **unprovable** — every closer (`auto`/`metis`/`blast`/reference `auto[1];blast`) then *hangs*, looking identical to a hard proof. **Diagnostic (no source needed):** set the proof to `apply deadlock_free` then `done` and read the residual goal — a missing disjunct for some `st` ⇒ that state has no enabled op ⇒ unprovable; `apply deadlock_free; sorry` under `-o quick_and_dirty` finishing in seconds confirms the *closer*, not the reduction, is the cost. **Fix:** give the controller no `Final` mode — reroute its terminal transition to a live state while still emitting the terminating event. Corollary: the gas typed-payload existential `params gs_input ∈ SeqGs` is NOT an obstacle — `auto` discharges it via `[simp] SeqGs = UNIV` once `Final` is gone; keep gas a faithful payload event. (This **refines/corrects** CLAUDE.md's "Final states are absorbing; the proof handles them via `St.exhaust_disc`" — untrue for the current FORKed generator + weak invariant.)

**F5 — Reduce a record/datatype payload's cardinality to fit the composed FDR4 check.** The two-controller `…_System_Module` explodes on `gas : LSeq(GasSensor,2)` (sequence depth 2 is fixed inside the vendored CSP generator, not tunable from our templates). The tractable lever is **GasSensor cardinality**: modeling `Chem` as a single-value enum drops `GasSensor` 4→2 values and the gas channel 21→7, taking FDR4 from a 64-min timeout (>774k processes still climbing) to ~17–22 min. Watch `_refines.exe` working set: a model that will finish *plateaus* (here ~17–18 GB then declines); one that won't climbs monotonically. (Contrast run-4 F6: ranges alone aren't always enough — payload cardinality is the second lever.)

**F6 — Only the primary (last-discovered) controller gets Dafny and Isabelle theories.** In a multi-controller study the Dafny/Isabelle generators emit artifacts for a single controller; the other is verified only through the composed FDR4 module. Concentrate Isabelle/Dafny-shaping effort on whichever controller is primary (here GA, scan-order last), and accept the secondary's design-by-contract / deadlock-freedom is asserted only at the CSP-composition level.

---

## chemical_detector — run-6 (condition B, severed-root base of fcf51d5, 2026-06-03)

> **⟳ Re-run 2026-06-05 — this is now the archived run-6.** Re-driven on the **fixed pipeline**
> (per-machine interfaces; FDR4 collapsed **8229 s → 4.9 s**) under clean isolation + **neutral
> CLAUDE.md**. Still **converged at iter-3** — the *same* count as this original leaky-CLAUDE.md
> run, so for run-6 the removed GasAnalysis answer did **not** change the iteration count (the only
> 2-iter figure was the discarded protocol-violation re-run). iter-1 hit the same `goreq`-arity
> (FDR4 Bool/Int + Dafny) and `deadlock_free` 611 s timeout (G-L) issues; converged iter-3 after
> fixing guards, Dafny postconditions, and a duplicate `SeqGs` constant. The entry below documents
> the superseded OLD-pipeline original; its findings remain valid.

Converged at **iter-3** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent. **Isolation: cleanest yet** — worktree on a parentless severed-root commit, so the run disclosed *no* `MEMORY.md`/`.remember` injection AND *no* `run-*`/`iter-*` ancestry in `git log` (the run-5 leak channel is closed). Ran against the F4-corrected CLAUDE.md (which it read as a canonical input, disclosed). Findings surfaced this run:

**F1 — Function and constant references must live on TRANSITIONS, not state ENTRY actions.** The ETL's function-discovery and the EGL's constant-collection both scan only `transition.condition` and `transition.action`. A sensor-function call or constant-bearing action (`move(lv,a)`, `wait(evadeTime)`) at the *head of a mode block* becomes a RoboChart **state entry action**, which neither pass scans → name never declared → CSP-gen aborts "Couldn't resolve reference to NamedExpression". Fix: keep entry actions to **events** (`send X` is registered during entry extraction); put any statement referencing a function/constant/multi-arg op on the **incoming transition(s)** instead. (Independently reconfirms run-4 F1 / run-5 F2.)

**F2 — Multi-arg operation calls are mis-modelled in entry actions.** `attachTopOfModeEntryActions` doesn't call `buildOperationCall`, so a 2-arg `move(lv,a)` as an entry action is captured by `buildCommunication` as a single-payload event (`event move : int`, dropping arg 2). The same call on a *transition* extracts correctly. Same root cause/fix as F1.

**F3 — The theory-generated (first-discovered) controller needs a bare-precondition op in EVERY state; a total guard cover is enough for FDR4 but NOT for Isabelle.** `IsabelleEgxRunner` picks the **first** `StateMachineDef`; its `by (metis St.exhaust_disc)` closer needs each state to have ≥1 transition with precondition exactly `st = X` (no guard conjunct). An autonomous-only mode covered by jointly-exhaustive guards passes FDR4 deadlock/divergence but makes `metis` **time out** (not fast-fail) on the residual `st = X ∧ guard`. Fix: an **event-triggered self-loop** (a `Tick` input) supplies the bare precondition with no τ-self-loop (no divergence), costing only determinism (unverified). **An `else { mode = same }` fallback does NOT help a total-guard-cover mode** — its precondition is the negation of the cover (vacuously false). To find which controller is first, check the generated `<Name>_Beh.thy` filename after `isabelle_gen`. (Sharpens run-5 F4: for autonomous-only + total-cover modes specifically, the `else→live-state` trick from run-5 doesn't apply; use Tick.)

**F4 — Controllers get no RoboChart `Final` node; model spec "final" states by rerouting to a live state.** Every mode-enum value becomes a plain `State`; the M2M never emits a `Final` junction for a controller, so a terminal dead-end mode compiles to CSP `STOP` = a real FDR4 deadlock. Reroute the spec's terminal transition back to a live state (still emitting the terminating event). Keeps FDR4 deadlock-free AND sidesteps the Isabelle "`Final`-state hang" entirely (no mode named `Final`). Confirms run-5 F4 and the CLAUDE.md correction made after run-5.

**F5 — Verify Isabelle/Dafny/coverage edits with `--phases` before paying for FDR4.** On this 2-controller study with `Seq(GasSensor)` state and `{0..1}` ranges, **FDR4 alone takes ~2.3–2.4 h** and dominates iteration wall-clock; the deterministic + generation phases are ~25 s and Isabelle ~10–35 s. When a change targets a non-FDR4 phase (e.g. the Tick/`deadlock_free` fix), confirm it with `run_experiment_iteration.py --phases t2m m2m m2t isabelle_gen isabelle_verify …` first; run the full pipeline only once for the convergence snapshot. (Note: this run measured FDR4 at ~2.4 h, much slower than run-4 F6's ~4 min and run-5 F5's ~17–22 min — the difference is `Seq(GasSensor)` cardinality + the C6 Sensors enum-leak inflating `{0..1}` input vars; keep the guard/state surface minimal.)

**F6 — A zero-arg CallExp in a guard can leak enum literals / clock fields into the `Sensors` interface as spurious `var … : nat`.** `noGas`/`gasD` (from `sts == Status.noGas` guards) and a clock field read in a guard leaked into `Sensors` because the M2M's zero-arg CallExp collector lacks the EGL's enum-literal awareness. Harmless to correctness (all phases pass; CSP-gen still resolves the guards to enum literals) but it **bloats the FDR4 state space** by adding `{0..1}` input variables — a real wall-clock cost on large compositions (see F5). Could not be removed from the Java side this run.

---

## sranger — run-4 (condition B, severed-root base of main, 2026-06-03)

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no prior-run ancestry; no `MEMORY.md`/`.remember` injection observed). sranger is the **timed** study (clock/elapsed-time guard). Pipeline time discounts iter-1's hung `isabelle_verify` (615s deadlock_free timeout) per the established convention. Findings:

**F1 — A `{0,1}`-range run cannot communicate any constant > 1 on a channel; the symptom is FDR4 "inconclusive", not a deadlock/divergence verdict.** The out-of-range value surfaces as `<channel>.….N is not a member of {0,1}` and makes *every* assertion inconclusive (0 passed) — easy to misread as a state-space/timeout problem. *Why:* the type-range correction pins channel value sets to `{0,1}` but constants keep their (ceiled) source value, so a payload of 2 isn't in the channel's set. *Apply:* on all-inconclusive FDR4, run `refines.exe --format framed_json` on the `*_coreassertions.csp` and read the `errors` field — if it names a channel value out of `{0,1}`, abstract the *communicated* constant down to ≤1 (magnitudes don't affect the checked properties); do **not** widen the range (RUN_TRAJECTORY §2 pins `{0,1}`). Constants used only in guard comparisons don't trigger this — only channel payloads do.

**F2 — A bare Tick self-loop on the terminal state is the surgical fix for the `deadlock_free` "Final" hang WHEN the machine has no typed-payload domain.** This **scopes/refines** CLAUDE.md's warning that "adding an operation on Final makes `apply deadlock_free` fail" — that warning applies to the *payload-domain* closer (`using St.exhaust_disc by auto`, the chemical_detector case). For an all-signal controller (SRanger) the generated theory uses `by (metis St.exhaust_disc)`, whose residual goal needs a **bare** `st = X` disjunct for every state; the terminal mode hangs only because it has *no* enabled op. A bare Tick self-loop (no extra guard) supplies the missing disjunct exactly like the other modes' self-loops and `metis` closes in seconds. *Apply:* before reaching for CLAUDE.md's heavier "remove Final / reroute terminal" fix, check the generated `.thy` for the closer in use — if it's `by (metis St.exhaust_disc)` (no typed-payload domain set), prefer a bare-precondition self-loop on the terminal state; it keeps all spec modes and absorbing-terminal semantics intact. (Contrast run-5/run-6 chemical_detector F4, which has a payload domain and reroutes instead.)

**F3 — Read the generated `.thy`/`.dfy` to diagnose; the enum-state-vs-RoboChart-`Final`-type distinction matters.** The ETL turns *every* mode-enum value into a regular `RoboChart!State` (a state merely *named* `Final` is not a RoboChart `Final` node); the EGL keys `Final` handling on `isTypeOf(Final)`, not the name. Reading `SRangerController_Beh.thy` (operations list + the `deadlock_free` lemma and its FORK comment) and `SRangerController.dfy` (the exact `ensures` + body) turned three opaque verifier failures into three one-line root causes *before* paying a 10-min Isabelle run. *Apply:* when a verifier fails, open its generated artefact and read the actual obligation/operation set rather than reasoning only from the Java.

**F4 — Gating a flattened autonomous transition on its cycle event fixes the Dafny "preempted postcondition".** When an else-if chain has a higher-priority event branch (e.g. `endTask`) above an autonomous guard branch, the ETL extracts the autonomous transition with its guard as a stand-alone postcondition (`guard ==> mode == target`) and loses the "not the higher-priority branch" context, so the event branch falsifies it. Gating the autonomous branch on the per-cycle Tick event (`event instanceof Tick && guard`) makes the generated premise event-specific and disjoint from the higher-priority event, discharging the contract. (Documented HOWTO §3.2 pattern; confirmed on the timed study.)

---

## sranger — run-5 (condition B, severed-root base of main, 2026-06-03)

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no `run-*`/`iter-*`/`archive` ancestry; no `MEMORY.md`/`.remember` injection). Pipeline time discounts iter-1's hung `isabelle_verify` (613s deadlock_free timeout). **Independence disclosure:** the canonical input `HOWTO_RUN_CONVERGENCE_EXPERIMENT.md` (§3.2) contains a sranger-flavoured worked-example note ("gate the autonomous on Tick … see sranger/trajectory.md iter 2"); the run states it diagnosed the same fix independently from the generated `.dfy` before recalling the note. Worth scrubbing/genericising that HOWTO hint so future sranger runs are cleanly cold. Findings:

**F1 — A single-controller study with a required terminal mode forces a "live sink" rewrite, and renaming the mode away from `Final` is the mechanism.** For a 1-controller study you cannot push `Final` onto a secondary controller (cf. chemical_detector). A state *named* `Final` is treated by the pipeline as a terminal needing no operation, yet the forked generator never designates it terminal — so it hangs with no op, and (per CLAUDE.md) fails the reduction *with* an op. Fix: rename the terminal mode to a non-`Final` name **and** give it one bare-precondition self-loop (here `tick`). Decisive: same structure named `Final` hung at 613 s; named `Stopped` it proved in 28.5 s. The self-loop alone isn't enough — the **name** must change too (a `Stopped` mode is structurally identical to `Moving`/`Turning`, which already prove with self-loops).

**F2 — The `Final` failure manifests as a per-goal `proof_timeout` on `*_deadlock_free`, not a fast tactic error.** `post_isabelle_verify` classifies it `isabelle_tactic_timeout` with `*** Timeout`. Apply: if `isabelle_verify` *times out* (≈ configured `proof_timeout`) on the `deadlock_free` lemma — as opposed to failing fast — suspect a state with no bare-precondition operation (an absorbing/`Final` mode) before touching guard logic. Keep `proof_timeout` finite so the hang is bounded and reported. (This is exactly the class of hang the pipeline-time discount excludes.)

**F3 — Gating an autonomous timed transition on the cycle `tick` fixes a higher-priority-event Dafny postcondition preemption.** The Dafny generator turns a transition's full guard (incl. any `event == X`) into the `ensures` premise; a bare autonomous guard yields `guard ==> target`, which any higher-priority event branch (`EndTask`) falsifies. Apply: if `post_dafny_verify` flags a `dafny_postcondition` on a `transitionFrom<Mode>` whose only guarded outgoing edge is autonomous, add `event instanceof Tick &&` to that guard, and pair it with a plain `tick` self-loop so `tick` keeps a total cover. (Matches sranger run-4 F4 and the HOWTO §3.2 pattern.)

**F4 — The preflight `rule4` annotation lint covers *every* `double`, including non-modelled `private static final` sentinels.** The iter-1 block was `DEFAULT_DISTANCE = 1000.0`, a constant that never reaches the RoboChart model. Apply: in cold codegen annotate **all** `double` fields/params/returns with `@RoboChartType("real")` up front (and `int`→`nat` where applicable), not only the ones you expect the M2M to lift — saves a whole preflight iteration.

**F5 — The clock encoding hinges on three exact Java shapes the ETL pattern-matches; get them right in iter-1 and the timed transition "just works".** (1) a dependency field whose **declared type is `Clock`** (or `@Clock`); (2) a state field assigned `field = clock.nowMs()` (→ promoted to a RoboChart `clock`, reset emitted as `# field` on its transition); (3) the elapsed guard written **exactly** as `clock.nowMs() - field >= CONST` (→ rewritten to `since(field) >= CONST`). Detection is by the receiver's *type*, not the field/method name, so the Clock-holding field can be named `timer` (avoids the reserved word `clock`). Produced a correct `since(clockResetTime) >= turnDuration` on the first try.

---

## sranger — run-6 (condition B, severed-root base of main, 2026-06-03)

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no `run-*`/`iter-*`/`archive` ancestry; no `MEMORY.md`/`.remember` injection). Pipeline time discounts iter-1's hung `isabelle_verify` (616.7s deadlock_free timeout). **Cleaner than run-5 on the HOWTO hint:** this run treated the HOWTO's reference to SRanger's top-level `trajectory.md`/iter-2 worked example as a §4 forbidden read and did **not** open it; all fixes derived independently. Findings:

**F1 — Advisory multi-real outputs collide with the `{0,1}` FDR type domain; prefer the single-payload `OutputEvent` entry-action encoding over multi-arg operation calls.** A 2-arg actuator call (`setMove(lv,av)`) is extracted by the M2M as an `LOperations` operation Call that pushes *both* argument values through a CSP channel typed `{0,1}`. Any output constant > 1 (`turnVel=2.0`) makes FDR4 abort with `... not a member of the set {0, 1}`, reported as `0 passed, N inconclusive` with **no Issues** — easy to misread as a hang. Fix: model outputs as `actuator.apply(new OutputEvent.X(v))` (single-payload; `extractOutputEventInfo` reads only constructor arg 0) at the top of the mode block (lifted to a State entry action); keep the primary component as the modelled payload and let the rest live only in Java. Diagnose via `refines.exe` directly on `*_coreassertions.csp`. (Combines sranger run-4 F1 with chemical_detector run-4 F2/run-5 F2; the *operation-call vs OutputEvent* choice is the lever.)

**F2 — A terminal `Final` mode breaks Isabelle `deadlock_free` but NOT FDR4.** Contrary to "no outgoing transition = CSP deadlock", in tock-CSP a mode with an entry action and no outgoing transition still passes FDR4 deadlock-free (time can pass via `tock`). The same mode makes `deadlock_free` **time out** (not fail fast) on `by (metis St.exhaust_disc)` because the FORKed generator's weak `tr ≠ []` invariant never *designates* a terminal, so the residual goal lacks a `st = <terminal>` disjunct. Fix (single-controller): give the terminal mode a non-`Final` name + a bare-precondition `Tick` self-loop (still emit the stop command on entry). Budget for it: presents as a ~10-min `isabelle_verify` (per-goal `proof_timeout=600`), not an instant error — which is exactly the class of hang excluded from productive pipeline time. (Confirms run-4 F2 / run-5 F1-F2; adds the explicit "FDR4 passes, Isabelle hangs" asymmetry.)

**F3 — The autonomous-transition postcondition is preempted by any higher-priority event branch in the same mode; gate it on the cycle event.** The Dafny generator emits per mode `<autonomous-guard> ==> mode == <target>` from the guard-only transition; if a higher-priority *event* branch (`EndTask`) sits above it in the `else-if` chain, then when event+guard both hold the event branch wins and the postcondition fails. Fix: conjoin the cycle/`Tick` trigger onto the autonomous guard (`event instanceof Tick && <guard>`). Generalises beyond sranger to *any* mode mixing a high-priority event exit with a guard-only exit. (Reconfirms run-4 F4 / run-5 F3.)

**F4 — Coverage `over_implementation` is suppressed per *file*, but framework exemptions are per *element name*.** The coverage check exempts framework *class* names (`Clock`, `RoboChartType`, `SensorService`, `RoboChartWait`) but **not their methods**: a `Clock` class with a public `now()` whose file has no trace entry flags `now` as over-implementation. Fix: ensure every `.java` file with a public type/method has **at least one** `result_codegen.json` entry (suppression keys on the file basename, so one mapped requirement suppresses all that file's members) — e.g. map a timing requirement to `Clock.java`.

---

## lre — run-4 (condition B, severed-root base of main, 2026-06-03)

> **⟳ Re-run 2026-06-05 — this is now the archived run-4.** Re-driven on **neutral CLAUDE.md**
> (closing the "LRE-flavoured guidance" leak this original disclosed) + fixed pipeline +
> scrub-before-commit + Tier-1-scrubbed tool files; clean config, no forbidden reads (audited).
> Same **iter-2**. FDR4 **8.5 → 0.34 min** — but lre is **single-machine**, so the per-machine fix
> is a no-op; the drop is **codegen variance** (a leaner model), not the pipeline. Per-iter tokens
> recorded in summary.json. Entry below kept as the superseded record.

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no `run-*`/`iter-*`/`archive` ancestry; no `MEMORY.md`/`.remember` injection). **No hung phase** — FDR4 (3/3) and Isabelle `deadlock_free` passed in *both* iters; the iter-1 failures were preflight (`@RoboChartType`) and Dafny postconditions, both fast. (Disclosure: CLAUDE.md, a canonical input, carries LRE-flavoured durable guidance — "condition B with the standing CLAUDE.md guidance".) Findings:

**F1 — The else-if-priority Dafny failure has two halves; the Tick gate fixes only one.** The HOWTO §3 directive ("gate the autonomous on Tick so its premise is event-specific") cures *event-branch-preempts-autonomous* overlaps. But a mode with **multiple autonomous transitions to different targets** also suffers *autonomous-preempts-autonomous* overlaps (here `inOpez`→OCM preempting `collision`→CAM and `hdist>1∧vdist>1`→MOM in HCM). The Tick gate does nothing for those — you must additionally conjoin `&& !<higher-priority-autonomous-guard>` onto each lower-priority autonomous branch. *Why:* Dafny extracts each branch's guard verbatim and asserts `guard ⟹ target` independently, so any two branches whose guards can co-occur with differing targets give one unprovable postcondition. *Apply:* within each mode make the guards pairwise disjoint **when the targets differ**; branches sharing a target (several →HCM) need no cross-negation. Clears all per-branch postconditions in one iteration. (Sharpens the generic "else-if priority" guidance: distinguish event-vs-autonomous from autonomous-vs-autonomous.)

**F2 — The M2M `[deadlock-lint]` advisory is a false-positive for event-triggered bare-precondition cover; trust `isabelle_verify`, not the lint.** After iter-2 the lint flagged **all four** modes ("no unconditional fallback → deadlock_free will fail"), yet the `deadlock_free` proof PASSED. *Why:* the lint only recognises an unconditional `else { mode = same }` as cover, but the preferred CLAUDE.md pattern-(b) cover — an event-triggered branch with no extra guard (`reqOCM` in MOM/HCM/CAM; `reqVel`/`reqHdng` in OCM) — also gives each state a bare-precondition op, which the proof accepts. *Apply:* do **not** add `else`-fallbacks to silence the lint — that injects a τ-self-loop that breaks FDR4 determinism for no benefit. Let `post_isabelle_verify.json` decide.

**F3 — Adding a real `Tick` *event* (not a Tick *self-loop*) is safe for FDR4 and Isabelle when the Tick transitions change mode.** Introducing `Tick` and gating mode-*changing* autonomous transitions on it kept FDR4 at 3/3 and left `deadlock_free` intact. *Why:* a Tick-triggered transition that *changes* mode is not a self-loop, so it creates no hidden-τ divergence cycle, and it doesn't disturb the existing event-triggered bare-precondition cover. This is the clean fix for the Dafny priority problem and is **distinct** from the "Tick *self-loop*" anti-pattern CLAUDE.md warns about (which competes with autonomous transitions and breaks determinism). (Cross-study nuance: chemical_detector/sranger used Tick *self-loops* on terminal/autonomous-only modes; LRE uses Tick as a real mode-changing trigger — both safe, different roles.)

**F4 — A phase reporting "executable not found" / "exit 127" is a tooling-path miss, not a verdict — fix and re-run before snapshotting.** `pipeline.yaml` ships `dafny_path`/`wsl_isabelle_bin` with a developer-specific username (`willr`); on another machine the Dafny/Isabelle phases "fail" with no real verification. *Apply:* before the first iter, sanity-check the win32 Dafny path and the WSL isabelle path; if a verify phase exits "not found" rather than a verdict, correct the local (uncommitted) path and re-run the *same* source (RUN_TRAJECTORY §5). (Recurs in every run on this machine — the committed `willr` paths; worth fixing the committed defaults or documenting the per-machine setup more prominently.)

---

## lre — run-5 (condition B, severed-root base of main, 2026-06-03)

> **⟳ Re-run 2026-06-05 — this is now the archived run-5.** Re-driven on neutral CLAUDE.md +
> fixed pipeline + scrub-before-commit + Tier-1-scrubbed tools; clean config, no forbidden reads
> (audited). Same **iter-2**. lre is single-machine (per-machine fix a no-op); FDR4 ~0.6 min,
> pipeline ~4.8 min — comparable to the original (codegen variance, not the fix). Per-iter tokens
> recorded. Entry below kept as the superseded record.

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no ancestry/memory leak). No hung phase (small pipeline times; iter-1 failures were preflight, an `isabelle_gen` *crash*, and Dafny — all fast). Surfaced a new pipeline gap now logged as **G-J** in `docs/fixes/pipeline_fidelity_gaps.md`. Findings:

**F1 — A `record` whose field is a `List<X>`/`Seq` crashes the Isabelle theory generator; make it a plain class if the controller never reasons about it formally.** `thy_generation_rule.egl` (record-field loop, ~line 240) assumes every datatype field's type is a `TypeRef` and reads `.ref`; a `SeqType` field throws `Property 'ref' not found … SeqType` at **generation** (`isabelle_gen`), not verification — and the captured feedback truncates right after the event-name dump, so the cause is only visible by running the gradle `isabelle_gen` task directly. *Why:* the M2M emits a `datatype` for every Java `record` (except event/sensor records); a `List<X>` component becomes a `Seq(X)` field. *Apply:* for collection-backed "registry/store" types that exist only as sensor/internal plumbing (never referenced in a controller guard or state var), declare them as a `final class`, not a `record` — they then never enter the formal model. Keep genuine scalar value-types (all-`real`/`nat`/`bool`) as records. (Logged as pipeline gap G-J.)

**F2 — Dafny per-guard postconditions require BOTH event-gating AND autonomous-guard negation when a mode has multiple overlapping autonomous transitions to different targets.** The generator emits `G_i ==> mode==target_i` per branch with no priority encoding. HOWTO §3.2's "gate the autonomous on Tick" fixes only *event*-preemption. When one mode has several autonomous transitions to *different* targets that can be simultaneously enabled (LRE MOM: `inOpez→OCM`, `collision→CAM`, `near-static→HCM`), you ALSO need `!(higher autonomous guards with other targets)` conjoined onto the lower branches. *Apply:* (a) add a `Tick` event, gate all guard-only transitions on it; (b) within each mode conjoin negations of earlier autonomous guards that lead to a *different* target; same-target lower branches need none; a mode whose every branch shares one target (CAM→OCM) needs no change. Express via named booleans to preserve the "named predicates only" codegen rule. (Independently reconfirms lre run-4 F1.)

**F3 — `Tick`-gating autonomous transitions is a triple win, not just a Dafny fix.** Converting guard-only transitions into `Tick`-*triggered* ones removes every τ-transition: satisfies the Dafny obligation (F2), keeps FDR4 deadlock/divergence-free (no guard-only cycles; visible `Tick` ≠ internal), and is compatible with `deadlock_free` as long as each mode keeps a bare-precondition *operator-event* transition (here every mode has a no-guard `reqOCM`/`reqVel`). Doesn't conflict with CLAUDE.md's "prefer event-triggered bare-precondition (pattern b)". (Confirms lre run-4 F3: Tick-as-mode-changing-event, distinct from the Tick-self-loop anti-pattern.)

**F4 — Nested sensor-function calls as *arguments* are accepted by the whole pipeline; nested calls as *callees* are not.** `sensor.odist(sensor.closestStaticIndex())` (a unary sensor function applied to a zero-arg sensor function's result) passes M2M/CSP-gen/Dafny/FDR4. The "function-typed variable application"/"method chain" crashes the docs warn about are about the *callee* being a non-simple expression (`a().b()`, target is an invocation), which `extractComputeBody` aborts on — not a call in *argument* position. *Apply:* freely nest sensor calls in argument position to avoid threading inter-operation index state, but never write `x().y()` method chains in a `compute()` body or guard.

**F5 — Committed machine-specific tool paths (`dafny_path`, `wsl_isabelle_bin`) are a per-machine setup trap that surfaces as a verifier "failure".** A wrong username in the committed `pipeline.yaml` makes `dafny_verify` ("Dafny executable not found") and `isabelle_verify` (WSL `exit 127`) fail in a way that *looks like* a verdict. *Apply:* verify both paths (alongside the §2 FDR4 `timeout`/`memory_limit_mb`) as setup before iter-1 (`where dafny`; `wsl -e bash -lc 'ls ~/isabelle/*/bin/isabelle; whoami'`); treat such "failures" as tooling, not code. (Recurs in *every* run on this machine — the committed `willr` defaults; strong candidate to fix the committed defaults or hoist the per-machine setup into RUN_TRAJECTORY §2 more prominently.)

---

## lre — run-6 (condition B, severed-root base of main, 2026-06-03)

> **⟳ Re-run 2026-06-05 — this is now the archived run-6.** Re-driven on **neutral CLAUDE.md** +
> fixed pipeline + scrub-before-commit + Tier-1-scrubbed tools; clean config, no forbidden reads
> (audited). **Converged at iter-3** (one MORE than this leaky-CLAUDE.md original's iter-2) and hit
> an isabelle_verify `deadlock_free` timeout (~12 min raw, not discounted). **Clearest evidence so
> far that the removed LRE-flavoured CLAUDE.md guidance materially aided convergence** — on neutral
> guidance the run needed an extra iter and hit the Isabelle hang the original avoided. Single-machine,
> so the per-machine fix is a no-op. Per-iter tokens recorded. Entry below kept as the superseded record.

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no ancestry/memory leak; whole `docs/` tree treated off-limits). No hung phase. Surfaced a new pipeline gap now logged as **G-K**. Findings:

**F1 — Every `double` needs `@RoboChartType("real")`, including constants, record components, and method parameters.** The preflight `rule4` lint does *not* infer `double → real`; it flags each unannotated `double` as a hard error (15 of iter-1's errors here). *Apply:* in cold codegen annotate **all** doubles up front — not just controller/sensor state fields. Record components accept the annotation (propagates to the backing field via `@Target FIELD`) and compile fine. (Reconfirms the recurring preflight-rule4 lesson across lre/sranger; the single most reliable "free iteration" to avoid.)

**F2 — A sensor/operation function must appear as a *top-level* expression at least once, or it is never declared.** The ETL's `collectFunctionNames` (`java2robochart.etl:1923`) recurses into boolean connectives and comparisons but **not** arithmetic (`*`,`+`) or unary negation. A parameterised sensor call used *only* nested inside arithmetic in an operation body (`tcpa = -(nsRelDist(i)*obsNsVel(i)+…)`) is silently never emitted as a `function …{ }`, and CSP-gen then aborts with "Couldn't resolve reference to NamedExpression". *Apply:* in any `compute()` that consumes a parameterised accessor inside arithmetic, first assign that accessor to its own field on its own line (`relNs = nsRelDist(i);`) — a bare assignment RHS *is* collected — then do arithmetic over the fields. (Functions that also appear in a controller guard, like `odist`/`hdist`/`vdist`, are already discovered via the guard path.) **Logged as pipeline gap G-K.**

**F3 — Gate autonomous transitions on a dedicated `Tick` event AND make cross-target autonomous guards disjoint.** The Dafny generator emits one `guard ==> mode == target` per transition with no else-if negations, so (a) a higher-priority *operator event* branch preempts a coincidentally-true autonomous guard, and (b) a higher-priority *autonomous* branch with a *different* target preempts a lower one. Fix (a): trigger every autonomous transition on `event instanceof Tick`. Fix (b): add `!<higher-priority-different-target-guard>` conjuncts. **Key nuance:** branches with the **same** target need **no** mutual negation — only differing-target overlaps need it, minimising negations. (Third independent confirmation: lre run-4 F1, run-5 F2, run-6 F3 all converge on the same two-part rule.)

**F4 — A nested named predicate (boolean of other named predicates) inlines correctly into guards.** `collisionImminent = cdaBelowMinSafe && tcpaNonNeg` used as `!collisionImminent` is cascade-inlined by `translateSpoonExpr` (`java2robochart.etl:2506-2517` inlines initializers that are binary/unary operators, recursively). Keeps controller guards composed of named booleans (per the codegen rules) while letting you negate a compound condition — preferable to inlining `!(a && b)` by hand.

**F5 — LRE's "operations write Ctrl_State, guards read it" shape is fully supported and the operation-call preamble in `step()` is intentionally dropped.** The ETL declares an `OperationDef` per `compute()` class and lifts non-final controller fields into `Ctrl_State`; the `op.compute(); this.x = op.x();` statements before the if-else chain are *not* extracted (only the if-else is walked). So caching operation outputs into controller fields is the correct way to (i) satisfy `LRE-Var*` "controller maintains" requirements with real Java fields and (ii) give guards named state variables to read, without the preamble polluting the state machine.

---

## sranger — run-7 (condition B, severed-root base of main, 2026-06-04)

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no ancestry/memory leak). No hung phase. **First run under the new §6.2a token-recording rule:** run-level Claude token total recorded in `trajectory.md` (summed from the session transcript JSONL since `/cost` isn't agent-exposed) — **output 237,443; total input incl. cache 18,438,990** (whole-run, not per-iter). **Independence disclosures (for the content audit):** (a) the HOWTO (a canonical input) §3 cites the *original sranger trajectory* as the worked example for the "gate the autonomous on Tick" fix — applied here only in *response to* the observed Dafny failure, not preemptively (iter-1 implemented SR-Beh5's autonomous transition faithfully and failed); (b) `thy_generation_rule.egl:953` carries a sranger-shaped comment example (`since(clockResetTime) >= turnduration`), seen while grepping the tool for Final-state handling. Both are candidates to genericise so sranger runs are cleanly cold. Findings (all confirmations of prior sranger lessons — no new pipeline gap):

**F1 — An out-of-range constant on a channel payload makes FDR4 report "N inconclusive, 0 errors" with an *empty* issue list.** `post_fdr4.md` carries no raw output for load-time errors, so the verdict looks like state-space exhaustion when it is actually a hard load failure (`The value: <chan>.x.y … not a member of the set {0, 1}`). FDR aborts before checking any assertion; the runner classifies unseen assertions as inconclusive. *Apply:* on `0 passed, N inconclusive` in ~1 s, re-run `refines.exe` manually on the discovered `*_coreassertions.csp` and read the raw error; then check every constant reaching a channel/operation argument against `{0..1}` (channel/LOperations-call payloads must be in range; guard-only constants are safe via saturating arithmetic). (Reconfirms sranger run-4 F1 / run-6 F1.)

**F2 — Design the terminal mode out of existence at cold-codegen time.** Applying CLAUDE.md's no-Final-state rule *in iter 1* (rename to a live mode `HALTED` + Tick self-loop) made Isabelle pass on the very first pipeline run (10/10 lemmas) — the inlined CLAUDE.md guidance is sufficient, no `docs/` access needed. *Apply:* for any study whose spec has a terminal/Final mode, encode it from cold as an ordinary named mode with an event-triggered self-loop and the stop command as its entry action; trace it to the Final-state requirements and flag the rename in `post_codegen`. (Shows the CLAUDE.md F4 correction now lets a cold run get Isabelle green first-try — the run-5/run-6 `Final`-hang is fully designed out.)

**F3 — Preflight rule4 wants `@RoboChartType("real")` on literally every `double`** — private statics in helper classes, clock-class fields, and method *parameters* (`Clock.advance#dt`) are all flagged even though none surface in the model. *Apply:* annotate every `double` field/parameter/return in the package mechanically at cold-codegen time; cheaper than an iteration. (Recurs in every lre/sranger run.)

**F4 — The machine environment can fail every gradle phase before any verifier runs; fix and re-run without snapshotting.** A foreign `GRADLE_USER_HOME` (here pointing at a non-existent `D:\repos\repository` → "Could not create parent directory for lock file") or `dafny_path`/`wsl_isabelle_bin` committed for another machine produces a uniform all-phase failure with zero verifier signal. *Apply:* treat a run where compile/preflight/t2m all fail with the same wrapper/lock exception as a pipeline crash (runbook §5): override `GRADLE_USER_HOME` per invocation, localise the tool paths, re-run, snapshot only iterations with real verdicts. (Extends the recurring `willr` tool-path trap with a new `GRADLE_USER_HOME` variant.)

---

## sranger — run-8 (condition B, severed-root base of main, 2026-06-04)

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base. No hung phase. **Run-level token total** (transcript-JSONL sum): output **319,092**; total input incl. cache **20,255,678** (152 assistant messages). For comparison run-7 was output 237,443 — two ~2-iter sranger runs now bracket ≈237k–319k output tokens. Minor independence note: the git-status snapshot exposed only scrub-*deletion paths* (chemical_detector cold-baseline filenames) — no sranger content, no iter counts; judged clean for B. Findings (all confirmations — no new pipeline gap):

**F1 — Spec constants that ride a channel must fit the FDR4 type ranges, and only a Java-side change can fix them.** The CSP generator inlines the `.rct` Constants defaults as `let`-bound `const_<Stm>_<name>` values inside `*_coreassertions.csp`. The pre-FDR4 correction step (`apply_csp_corrections` + `csp_overrides.csp`) rewrites only `-- generate` blocks in `instantiations.csp`, so it **cannot** reach those constants. Under `{0..1}`, any constant > 1 passed as an event/operation payload (e.g. a velocity to an actuator call) kills every assertion at load. *Apply:* scale payload-carried constants into range at cold codegen (document the deviation); guard-only constants (thresholds, durations) merely make guards trivially true/false and don't error. (Sharpens run-7 F1 with the *why-csp_overrides-can't-fix-it* detail.)

**F2 — `post_fdr4` "N inconclusive" with an empty issues list means the CSP failed to *load*, and the feedback layer won't tell you why.** A channel-range violation (F1) aborts evaluation before any assertion runs; the runner classifies it "0 passed, N inconclusive, 0 errors" with no issue/fix-directive and a misleadingly tiny ~1 s wall-clock. *Apply:* on inconclusive FDR4, re-run `refines.exe` manually on the file named by the "auto-discovered" line in `post_fdr4.md` and read the raw error; don't guess from the empty structured feedback. (Manual *diagnostic* runs are fine; never interrupt the pipeline's own live FDR4 run, per RUN_TRAJECTORY §2.)

**F3 — Preflight rule4 is stricter than the codegen-rules text reads: EVERY `double` field/parameter needs `@RoboChartType("real")`, including private constants and framework-facing setter params that never reach the model** (`Sensor.update(double)`, `Clock.setTime(double)`, a private `static final` default). *Apply:* annotate every `double` declaration site mechanically during cold codegen. (Recurs in every lre/sranger run — strongest candidate for a codegen-rules text clarification or an auto-annotate codegen pass.)

**F4 — (Confirmation) The two documented patterns fired exactly as the canonical inputs describe, and applying them proactively/on-cue is what made this a 2-iter run.** (a) CLAUDE.md's "no `Final` state on the theory-generated controller" — implemented at iter 1 as a *renamed* ordinary absorbing state (`Halted` + no-action tick self-loop + entry `move(0,0)`), keeping the spec's terminal semantics and letting the Isabelle deadlock-freedom proof pass on first contact, **no rerouting to a live operational state needed** (cf. chemical_detector, which reroutes). (b) HOWTO §3's Dafny autonomous-preemption fix (gate the autonomous transition on `Tick`) — the iter-1 failure matched the documented symptom verbatim and the fix resolved it in one edit. This is the fourth consecutive sranger 2-iter convergence (run-4/5/6/7/8 — five total) — the timed-study path is now a fully solved, two-edit pattern.

---

## lre — run-7 (condition B, severed-root base of main, 2026-06-04)

> **⟳ Re-run 2026-06-05 — this is now the archived run-7.** Re-driven on **neutral CLAUDE.md** +
> fixed pipeline + scrub-before-commit + Tier-1-scrubbed tools; clean config, no forbidden reads
> (audited). Same **iter-2**, but hit an isabelle_verify `deadlock_free` timeout (~12 min raw) that
> the leaky-CLAUDE.md original avoided (isaV 2.2 min) — same leak-helped pattern as run-6. Single-
> machine (per-machine fix no-op). Per-iter tokens recorded. Entry below kept as the superseded record.

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base (no ancestry/memory leak; nothing under `docs/` read). No hung phase. **Run-level token total:** output **318,026**; total input incl. cache **~26.5 M** (145 assistant messages). (lre run-7 ≈ sranger run-8 ≈ 318–319k output; sranger run-7 was 237k.) Findings (confirmations — no new pipeline gap):

**F1 — The Dafny generator's ensures are unconditioned per-branch implications; autonomous guards must be event-gated AND pairwise exclusive.** For every guarded branch it emits `ensures <event &&> guard ==> mode == target` with *no* else-if negations. Tick-gating (HOWTO §3) fixes only the event-path violations; two *autonomous* guards simultaneously true with different targets still refute each other's ensures on the shared `Tick` path (`ensures inOpez ==> mode == OCM` fails on the `mode := CAM` path). *Apply:* when a mode has ≥2 autonomous transitions to *different* targets, give the lower-priority branch explicit negations of every higher-priority same-trigger guard (`!camRisk`, `!inOpez`), ideally one named predicate per higher-priority guard so the negation is a single `!name`; same-target branches need none. **This makes the "redundant" negation workaround (fdr4_system.txt option 3) *mandatory* for Dafny convergence, not optional.** (Third independent lre confirmation: run-4 F1, run-5 F2, run-6 F3, run-7 F1 — fully stable rule.)

**F2 — Read the pipeline sources before cold codegen; the extraction contract is derivable from the tool.** ~30 min reading `java2robochart.etl`, `robochart2rct.egl`, `StructuralLinter.java` (all §1-legal) fixed the design up front: (a) operation `compute()` output fields must *reuse the controller's Ctrl_State field names* (the .rct emitter never renders operation-local vars; assignments resolve against `requires Ctrl_State` — name-matching is the contract); (b) cache operation results into non-final fields at the top of `step()` ("state-var lift") and write predicates over fields — the M2M ignores those statements and they populate Ctrl_State; (c) `actuator.apply(new OutputEvent.AdvVel(x))` is the recognised Communication idiom; (d) float literals render as ceiled integers → keep spec values integer-valued; (e) `@RoboChartType` is consumed by the *linter* (doubles must carry `"real"`), not the ETL (`int`→`nat` unconditionally). Result: 11/12 phases green on the cold iter.

**F3 — Stale `GRADLE_USER_HOME` fails every gradle phase identically; treat as environment crash, not an iter.** Each java/gradle phase dies in ~0.1 s with `Could not create parent directory for lock file <old-drive>\…`. *Apply:* check `$env:GRADLE_USER_HOME` before the first run; override per-invocation if it points at a dead path; fix and re-run without snapshotting (§5). (Same `D:\repos\repository` trap as sranger run-7/8 — a machine-level env issue, not pipeline.)

**F4 — Dafny (6a) only models the controller; operations and sensor bodies are abstracted away.** The generated `.dfy` has the mode datatype, Ctrl_State fields as havoc variables, uninterpreted `reads this` functions for guard-used sensor methods, and one transition method per mode — `compute()` bodies and the Sensor implementation never appear. So compute()-body complexity (division, CPA math) can't fail 6a; what fails 6a is purely the *step() branch structure vs the generated ensures* (F1). *Apply:* effort to make operation bodies verifier-friendly should target the M2M/RCT path (no locals, single expressions), not Dafny. (Useful scoping clarification of what each verifier actually checks.)

**F5 — An event-triggered pass-through self-loop doubles as the bare-precondition op.** OCM's `reqVel`/`reqHdng` pass-through branches (typed trigger + `advVel ! v` action) satisfy the Isabelle bare-precondition requirement for the initial mode with zero extra machinery — no `else { mode = same; }`, no τ-loop, FDR4 divergence-freedom preserved. *Generalises:* if a mode must handle *any* unguarded event anyway, that branch is its deadlock-freedom cover. (Complements CLAUDE.md pattern-(b); shows OCM needs no special bare-precondition device.)

---

## lre — run-8 (condition B, severed-root base of main, 2026-06-04)

> **⟳ Re-run TWICE; this is now the archived run-8 (clean redo, 2026-06-05).** Supersedes both
> the original (iter-3, leaky CLAUDE.md) AND an interim redo (iter-2) that used the weaker
> commit-tree+scrub-in-worktree isolation (gitStatus path exposure) on un-scrubbed tool files.
> This final redo uses the **clean scrub-before-commit base + Tier-1-scrubbed tool files + neutral
> CLAUDE.md** (matching lre 4–7); clean config, no forbidden reads (audited). **Converged iter-3**
> (the two neutral redos varied 2↔3 iters — codegen variance), genuinely (iter-3 all 12 phases
> pass; isaV a real 1-min/20-lemma pass). **No `deadlock_free` hang** here (isaV 3.2 min total) —
> unlike lre run-6/7, so the leak-helped-Isabelle pattern is not universal. Single-machine, so the
> per-machine fix is a no-op; FDR4 ~0.5 min (codegen variance). Per-iter tokens recorded. The entry
> below documents the **superseded original** and its findings remain valid as that record.

Converged at **iter-3** (12/12 phases pass, vacuity 0) — the **first lre run this batch to need 3 iterations** (run-4/5/6/7 were all 2). Actor `me-as-developer`, honest-independent on a parentless severed-root base. **No hung phase** — FDR4 passed all three iters, but each took **~1190–1230 s** (vs lre run-7's ~100 s), dominating the ~1280–1330 s pipeline totals; a much larger FDR4 state space this run (more state vars + the `ObstacleRegister` TreeMap-backed sensor + 13 named predicates). Productive = raw. **Run-level token total:** output **342,588** (highest yet — 3 iters); total input incl. cache 22,985,569 (156 assistant messages). Findings (refinements/confirmations — no new pipeline gap):

**F1 — The Dafny ensures framing problem has TWO distinct halves, and the second is what cost the 3rd iter.** Each guarded Java branch becomes `ensures <guard> ==> mode == <target>` verbatim, no path negations. To verify: **(a)** same-mode branches with *different* targets must be made mutually exclusive in Java (conjoin negations of higher-priority guards — redundant under else-if, harmless to the other verifiers since the ETL extracts guards independently); **(b)** any guard applying a *sensor function* must *additionally* be Tick-gated, because functions are generated as uninterpreted `reads this` functions — once the body assigns `mode` the heap changes and their post-state values are **unframed**, so only a *framed* conjunct (a plain field, or the immutable `event` parameter) can falsify the premise on other paths. **Diagnostic:** the ensures that already verify are exactly the field-only and `event ==`-conjunct ones; the failing return paths are operator-event branches. (This *sharpens* run-7 F1: mutual-exclusion negations alone fixed 2 of 3 errors here, but the field-vs-function *framing* distinction is why the sensor-function guards specifically needed Tick — a deeper mechanism than "event-gate + negate".)

**F2 — Annotate `@RoboChartType("real")` on every `double` at codegen time** — record components, public static-final constants, and private static-final helpers all count (preflight rule4 checks all double fields). Cost one full iter here. (Universal across lre/sranger.)

**F3 — Bare-precondition cover via guard-free event branches works first try.** Every mode with ≥1 event-triggered guard-free branch (reqOCM/reqVel), no `else` fallback self-loops, no `Final` state → FDR4 (deadlock+divergence) and Isabelle `deadlock_free` passed from iter 1 and stayed green through all edits. CLAUDE.md guidance is sufficient; treat as a hard codegen invariant, not feedback-driven repair.

**F4 — Operations are abstracted out of the Dafny model, so operation-internal numerics can't block convergence.** The `.dfy` models only the controller (state vars + uninterpreted sensor functions); `compute()` bodies — including a potential CPA division-by-zero — never reach the verifier. Don't spend cold-codegen effort defending operation arithmetic for Dafny; spend it on guard/encoding structure. (The CPA div-by-zero is a *runtime* NaN concern — handled here via an odist-based CPA identity whose NaN outcome fails all CPA guards safely.) (Confirms run-7 F4.)

**F5 — A `GRADLE_USER_HOME` pointing at a nonexistent drive fails every gradle-backed phase with "Could not create parent directory for lock file <path>.lck".** Recognise as infrastructure, not code: fix the env var (per-process override suffices), re-run; the crashed run is not an iter and must not be snapshotted (§5). (Same `D:` trap as sranger run-7/8, lre run-7 — a persistent machine-env issue worth fixing at the machine level once.)

---

## chemical_detector — run-7 (condition B, severed-root base of main, 2026-06-04)

> **⟳ Re-run 2026-06-05 — this is now the archived run-7.** Re-driven on the **fixed pipeline +
> neutral CLAUDE.md** with scrub-before-commit isolation (zero gitStatus path exposure). Same
> **iter-2**; FDR4 collapsed **18.7 → 0.06 min** (per-machine fix), but iter-1's `deadlock_free`
> 600 s timeout (the G-L Status-enum total-cover/metis hang) **persists** — unrelated to the
> per-machine fix — so isaV stays ~10.8 min (kept raw). Per-iter tokens recorded in summary.json.
> **Disclosed exposure (legitimacy):** the run read sanctioned tool/setup files that name
> chemdetector specifics — `pipeline.yaml`'s fdr4 "historical note" (prior cd run reached iter-9 +
> the InputEnv/OutputEnv→M2M fix), and ETL/coverage.py/HOWTO comments naming GasAnalysis/Obstacle;
> the agent judged none consumed as a fix recipe. Same leak-class as the CLAUDE.md genericization;
> a candidate tool-file scrub. Entry below kept as the superseded record.

Converged at **iter-2** (12/12 phases pass, vacuity 0) — **fastest chemical_detector run to date** (run-4/5/6 were 3/6/3). Actor `me-as-developer`, honest-independent on a parentless severed-root base. Pipeline time discounts iter-1's hung `isabelle_verify` (612s deadlock_free timeout — the F2 metis hang); iter-2's FDR4 1117.9s is a *passing* two-controller-composition run (~19 min), not discounted. **Run-level token total:** output **320,208**; total input incl. cache ~19.3M (133 usage blocks). Two-line iter-2 fix. Findings:

**F1 — Sensor methods referenced from guards must take at most one argument.** A 2-arg sensor call in a named predicate fails three ways at once: the ETL can't inline it → falls back to a `var <predicate> : nat` Sensors variable (FDR4 dies with Bool/Int mismatch on the guard); the Dafny generator emits the function with only its first parameter while call sites keep all args (arity errors); and the opaque guard reaches the Isabelle theory. *Apply:* express binary comparisons (thresholds, orderings) directly with comparison operators in the named predicate (`ins >= THR`), keeping multi-arg "spec functions" as plain Java methods for coverage only. CLAUDE.md's `function <name>(p : <Type>)` is literal: ONE parameter. (Same root as pipeline gap **G-A**; this is the controller-guard manifestation.)

**F2 — Total guard cover must be propositionally complementary, not merely enum-exhaustive — or `deadlock_free` hangs.** The closer `by (metis St.exhaust_disc)` only knows the *mode* enum's exhaustiveness. A guard pair `sts == noGas` / `sts == gasD` is jointly exhaustive only via the *Status* payload enum, so the residual `sts = noGas ∨ sts = gasD` is out of metis's reach and the tactic *hangs* (timeout indistinguishable from a hard proof — and the exact 612s iter-1 hang here). A pair written `p` / `!p` leaves the tautology `p ∨ ¬p`, which closes instantly. *Apply:* in every autonomous-only mode write the inner chain as `if (p) … else if (!p) …` (literal negation of the same named predicate); for 2-valued enums this is semantically identical to the two-literal spec form. **Logged as pipeline gap G-L.** (This is an important *refinement* of CLAUDE.md's "prefer a total guard cover for autonomous-only modes" guidance — the cover must be propositional, not enum-literal.)

**F3 — In a multi-controller study, check which controller is primary before deciding where Final-avoidance matters.** Here the Isabelle theory and Dafny contract were both generated for `GasAnalysisController` (not Movement). Keeping *both* controllers Final-free cost little and made the choice irrelevant — recommended default, since it also keeps the FDR4 composition free of terminated (SKIP) component processes. (Confirms run-5/6 F4/F6; adds the "keep both Final-free" default.)

**F4 — Stale `GRADLE_USER_HOME` kills every gradle phase with "Could not create parent directory for lock file <drive>:\…".** Check it before the first run and override per-invocation (`$env:GRADLE_USER_HOME = "$HOME\.gradle"`) rather than editing the user's persistent env. Signature: compile fails in 0.2 s and every java/gradle phase repeats the same lock-file path. (Same `D:` trap as lre run-7/8, sranger run-7/8 — now seen in all three studies; strong candidate to fix at the machine level.)

**F5 — Operation-call arity determines the RoboChart construct.** Zero-arg action calls (`randomWalk()`) → untyped `send` events; 1-arg (`changeDirection(l)`) → *typed output events* (`event changeDirection : Loc`); ≥2-arg (`move(lv,a)`) → LOperations `Call`s. The spec's single word "operation" maps to three different formal constructs — useful when reading the .rct against requirements, and the lever behind run-4 F2 / run-6 F2 (the OutputEvent-vs-operation-call typing choice).

**F6 — Harmless ETL fallback noise in the Sensors interface.** Unresolvable identifiers from guards (enum literals `noGas`/`gasD`, the clock field `t`) appear as junk `var <name> : nat` entries in `interface Sensors`. They type-check and FDR4/Isabelle ignore them; **don't burn an iteration removing them.** (Same surface as run-6 C6/F-Sensors-leak / pipeline gap G-H — confirmed harmless to correctness.)

---

## chemical_detector — run-8 (condition B, severed-root base of main, 2026-06-04)

> **⟳ Re-run 2026-06-05 — this is now the archived run-8.** Re-driven on the **fixed pipeline +
> neutral CLAUDE.md + Tier-1-scrubbed tool files**, scrub-before-commit isolation, no forbidden
> reads (audited). **Converged at iter-1** — one better than this original's iter-2: it wrote
> `ins >= thr` directly from cold (avoiding the 2-arg `goreq` that this run fixed in iter-2) *and*
> the `p`/`!p` propositional cover + no-Final, so all 12 phases passed on the first pipeline run.
> FDR4 18.7 → 0.04 min (per-machine fix). The *ideal-case* run — strict adherence to
> java_codegen_rules.txt (named predicates + `!` negation) yields first-try convergence; note
> end-to-end was still 63 min (careful cold codegen, not repair). Per-iter tokens in summary.json.
> Entry below kept as the superseded record.

Converged at **iter-2** (12/12 phases pass, vacuity 0). Actor `me-as-developer`, honest-independent on a parentless severed-root base. **First chemical_detector run with NO Isabelle hang** — by applying the `p`/`!p` complementary guard cover (G-L) and the no-Final rule *from cold codegen*, `deadlock_free` passed on the very first pipeline run (9 lemmas), so the only failures were the 2-arg `goreq` (FDR4 Bool/Int load error + Dafny arity ×4), fixed by a single `goreq(ins,THR)`→`ins >= THR` edit. No hung phase; iter-2 FDR4 = 1121s (passing, ~19 min). **Run-level token total:** output **268,222**; total input incl. cache ~25.3M (160 assistant messages). Findings (confirmations — no new pipeline gap):

**F1 — Never reference a multi-argument function from a controller guard predicate; use the direct comparison.** Function emission is single-parameter end-to-end: Dafny declares `function <f>(p0:T)` regardless of Java arity (→ "wrong number of arguments" at every call site) and the CSP path types the surviving predicate variable as `Int` not `Bool` (→ FDR4 "Couldn't match expected type Bool with actual type Int"). *Apply:* guard predicates should be binary-op comparisons over state vars/constants (these inline faithfully, `ins >= thr`) or 0/1-arg sensor calls; keep multi-arg invocations to transition *actions* where the LOperations Call path handles them (`vehicle.move(LV,a)` works). (Same root as pipeline gap **G-A** / run-7 F1; this is the third confirmation that a 2-arg guard function breaks *two* verifiers at once.)

**F2 — The Isabelle theory targets the *first* StateMachineDef, not a declared "primary".** `IsabelleEgxRunner.findStateMachineName` walks `getAllContents()` and returns the first match (here `GasAnalysisController`) — **note CLAUDE.md says "last-discovered", which appears inaccurate.** Robust move in any multi-controller study: make *every* controller theory-safe (no Final mode anywhere; every mode carrying either a bare-precondition event trigger or a complementary guard cover) rather than betting on discovery order. (Flag: CLAUDE.md first-vs-last wording is a candidate correction.)

**F3 — Express exhaustive autonomous guards as `p` / `!p` over one named predicate, not two independent positive guards.** On a 2-valued domain enum `!(sts == noGas)` ⟺ `sts == gasD`, so the complementary form is spec-faithful AND (a) a total guard cover for FDR4 deadlock/divergence, (b) closes the Z-machine `deadlock_free` proof as a propositional tautology — `by (metis St.exhaust_disc)` succeeded with both an inlined comparison pair and a named boolean pair, with no need for the domain enum's exhaust fact. Writing the second branch as `else if (!p)` (not bare `else`) keeps the extracted guard explicit. (Confirms run-7 F2 / pipeline gap **G-L**, and is what made this the first hang-free cd run.)

**F4 — When removing a Final state, the rerouted sink must accept every shared event its still-live partner can emit.** `Found` self-loops on stop, resume, *and* turn: after GA's terminal transition is rerouted to Reading, GA keeps classifying readings and may emit any of the three shared events while MV sits in Found — refusing one risks a module-level deadlock. The 9/9 System_Module FDR4 pass confirms the shape. *Apply:* enumerate the partner's possible emissions in each rerouted sink state and self-loop on all of them. (New nuance on the no-Final reroute pattern — composition safety, not just per-controller.)

**F5 — "entry action receives an event" requirements (`odometer ? d0`) can be encoded as a zero-arg sensor read in the transition action.** `d0 = odometrySensor.odometer()` becomes an `odometer` Sensors-interface variable and an ordinary assignment action, accepted by all verifiers — sidesteps the step()-pattern's inability to consume a second event inside an entry action (at the model-fidelity cost noted in caveats). (Confirms the recurring odometer caveat across cd runs / pipeline gap G-B.)

---

## chemical_detector — run-4 (RE-RUN, condition B, severed-root base, 2026-06-04)

> **⟳ Re-run again 2026-06-05 — this is now the archived run-4.** The 06-04 re-run below was
> still on the **OLD pipeline + leaky CLAUDE.md** (FDR4 9/9 in 1230 s). Re-driven on the **fixed
> pipeline + neutral CLAUDE.md** (clean `CLAUDE_CONFIG_DIR`): same **iter-2** convergence, FDR4
> collapsed **20.5 → 0.04 min** (per-machine fix). Entry below kept as the superseded record.

**This replaces the original run-4** (first batch, 3 iters, linear base). Re-run on a severed-root base with the **scrub-before-commit** isolation fix. Converged at **iter-2** (12/12 phases pass, vacuity 0; FDR4 9/9 in 1230 s). No hung phase. **Run-level token total:** output **305,170**; ~20.2 M total incl. cache. Same lean path as cd run-7/8: Isabelle green on the *first* pipeline run (no-Final + complementary `p`/`!p` covers from cold), only the 2-arg `goreq` guard needing the iter-2 fix (`goreq(ins,thr)` → `ins >= thr`).

**META-FINDING (the load-bearing one) — a filename-exposure channel can shave an iteration; closing it restored the honest count.**
- An **earlier re-run of this same run-4**, built with the *old* setup (`git commit-tree HEAD^{tree}` then `rm` the §4 paths in the worktree), converged at **iter-1**. That setup leaves the scrub as **deletions in the worktree**, so the harness `gitStatus` SessionStart snapshot leaked the *deleted file paths* of `experiments/cold-baseline/chemical_detector/run-1/**` — i.e. prior chemdetector **filenames** (`GasAnalysisOutput.java`, `Vehicle.java`, `DetectorConstants.java`, `datatype/Angle.java`, the `post_*` names) — into the session context **before** codegen. The run honestly disclosed it (`condition-B-with-filename-exposure`).
- This **clean re-run**, built **scrub-before-commit** (build the scrubbed tree in a temp index, `git commit-tree` *that*), has a worktree with **zero deletions** — `gitStatus` shows only `M pipeline.yaml`. It converged at **iter-2**, matching cd run-7/8.
- *Interpretation:* the iter-1 result was at least partly enabled by the leaked structure hints (or run-to-run variance); the **honest, exposure-free count for cd cold-codegen under current guidance is 2 iters**. Don't report the iter-1 number.

**PROCESS FIX (now standard) — scrub BEFORE the severed base commit.** Build a scrubbed tree object via a temp index and `commit-tree` it, instead of committing the full tree and deleting in the worktree:
```
GIT_INDEX_FILE=/tmp/idx git read-tree HEAD
GIT_INDEX_FILE=/tmp/idx git rm -r --cached --ignore-unmatch <§4 paths>
TREE=$(GIT_INDEX_FILE=/tmp/idx git write-tree); BASE=$(git commit-tree "$TREE" -m "isolated run base")
```
The worktree then natively lacks the §4 paths (no deletions → nothing for `gitStatus` to leak). Closes the last disclosed contamination channel for the severed-root method (combines with the earlier `result_codegen.json` untrack). Recommend folding into RUN_TRAJECTORY Appendix.

Findings F1–F5 are confirmations of prior cd lessons (no new pipeline gap): F1 = 2-arg guard function breaks FDR4+Dafny (G-A); F2 = enum-literal/clock-field leak into Sensors (G-H, benign); F3 = no-Final + complementary `p`/`!p` design gets Isabelle green cold (G-L applied); F4 = coverage errors on any untraced public element; F5 = FDR4 is the order-of-magnitude iter-time cliff (1 s upstream-fail vs 1230 s passing composition).

---

## chemical_detector — run-5 (RE-RUN, condition B, scrub-before-commit base, 2026-06-04)

> **⟳ Re-run again 2026-06-05 — this is now the archived run-5.** The 06-04 re-run below was
> still OLD pipeline + leaky CLAUDE.md (FDR4 9/9 in 1013 s, iter-1 isabelle_verify 612 s hang).
> Re-driven on the **fixed pipeline + neutral CLAUDE.md** (scrub-before-commit, zero gitStatus
> exposure): same **iter-2**, FDR4 collapsed **16.9 → 0.06 min**, and **no isaV hang** this time
> (1.1 min). Per-iter token splits now recorded in each iter's summary.json. Entry below kept as
> the superseded record.

**This replaces the original run-5** (the 6-iter ordeal — the worst run in the dataset, from before the CLAUDE.md no-Final/bare-precondition corrections). Re-run on a scrub-before-commit severed base (no `gitStatus` exposure — clean). Converged at **iter-2** (12/12 pass, vacuity 0; FDR4 9/9 in 1013 s). Pipeline time discounts iter-1's hung `isabelle_verify` (612 s). **Run-level token total:** output **334,919**; ~28.3 M total incl. cache.

**HEADLINE — the CLAUDE.md correction cut run-5 from 6 iters to 2.** The original run-5 spent six iterations discovering the `Final`-state `deadlock_free` hang and its fix. This clean re-run *still hit the hang at iter-1* (cold codegen used a **guarded** `else if (sts == gasD)` cover — the G-L variant — so `metis`/`auto` couldn't dispatch the `Status`-enum disjunct), but the corrected CLAUDE.md + the `post_isabelle_verify` feedback told it the fix immediately, and **iter-2 converged** by converting the guarded `else if` to a bare `else` (giving each autonomous-only mode a bare-precondition op). Same 6→2 collapse the toolchain maturity predicts.

**New pipeline gap G-M** (typed-event self-loop mis-typing) logged in `docs/fixes/pipeline_fidelity_gaps.md`. Findings:

**F1 — Every `instanceof` branch on a payload-carrying event must capture the payload, even when the handler ignores it.** The ETL types a trigger only when the branch's first action is a state-var capture (`this.<sv> = cast.value()`). An absorb/ignore self-loop without the capture binds the trigger to the stm's default `var v : real`, clashing with the event's `Seq(GasSensor)` type → FDR4 `Couldn't match expected type Int with actual type <(Chem,Int)>`. *Apply:* even on a no-op/absorb self-loop, capture the payload into the matching state var (`this.gs = g.value()`) — usually spec-consistent. **Logged as pipeline gap G-M.** (New; surfaced because run-5 used a `Done` mode self-looping on `gas` to stay receptive after halt.)

**F2 — "Bare precondition" means *syntactically unguarded*; a total guard cover via guarded `else if` hangs `deadlock_free`.** This is the iter-1 hang: `Analysis` had only `sts==noGas` / `sts==gasD` (both guarded), and the closer has no `Status.exhaust_disc` fact, so the residual is undispatchable and `metis`/`auto` searches forever. Converting the second branch to a plain `else` makes its precondition exactly `st = Analysis` (bare) → closes instantly. *Apply:* every autonomous-only mode needs at least one branch whose precondition is *just* the source mode — write the complement as bare `else`, not `else if (!p)` guarded. (Confirms pipeline gap **G-L**; this is the codegen-side rule.)

**F3 — Zero-arg function discovery leaks enum literals and clock fields into the `Sensors` interface as spurious `var … : nat`.** `noGas`/`gasD`/`stuckClock` appear as junk Sensors vars; harmless (verification passes), don't burn an iteration removing them. (Confirms pipeline gap **G-H**.)

**F4 — Budget FDR4 well above the committed default for the two-controller composition.** Iter-2's passing FDR4 was 1013 s (~17 min) at `{0..1}`; the committed 600 s timeout would have killed it. The §2 policy (`timeout: 3600`, `memory_limit_mb` = page-file) is mandatory for this study. (Recurring cd lesson.)

**F5 — When rerouting spec final states to live modes in a multi-controller study, argue composition liveness explicitly.** `Done` (GA) and `Found` (MV) are entered on the same `stop` transition, so MV can only be in `Found` while GA is in `Done` (which emits no further shared events) → no unconsumed-shared-event deadlock; the 9/9 System_Module FDR4 pass confirms it. (Sharpens run-8 F4: the reroute is safe *because* the partner is quiescent in the paired sink state.)

---

## chemical_detector — run-9 (condition B, FIXED pipeline + NEUTRAL CLAUDE.md, 2026-06-05)

Converged at **iter-2** (12/12, vacuity 0). First cd run on **both** the per-machine-interface pipeline fix (`a9c9da6`) **and** the genericized study-neutral CLAUDE.md (`59b4432`). **No hung phase**; FDR4 9/9 in ≤2.6s (per-machine interfaces active). **Run-level token total:** output **387,935** (highest — iter-1 spent ~1h reading the ETL/EGL sources). **Headline for independence:** with the study-specific GasAnalysis/no-Final hints REMOVED from CLAUDE.md, the cold codegen *still* independently used no-Final + complementary `p/¬p` covers and avoided the 2-arg `goreq` guard — so the **only** iter-2 fix was the universal `@RoboChartType("real")` preflight (5 doubles). Strong evidence the removed study-specific hints were **not load-bearing**: the generic rules + reading the pipeline sources suffice. **No exposure** (scrub-before-commit + neutral CLAUDE.md). Findings (F3 is a NEW gap → G-N; rest confirmations):

**F1 — Read the transformation sources before cold codegen; they are the encoding contract.** ~1h reading `java2robochart.etl` + `robochart2rct.egl` in iter-1 prep surfaced every encoding rule and produced a model spec-faithful on the first extraction → 2-iter convergence with **zero model-level failures**. Treat it as a standing iter-1 step ("the tool, not the answer key").

**F2 — M2M function-signature inference is first-parameter-only** (pipeline gap G-A). A multi-arg Java method referenced from a guard/action emits as a 1-param RoboChart function → arity mismatch. Inline binary comparisons (`ins >= thr`) in named predicates; reserve guard method-calls for single-parameter functions.

**F3 — A controller ctor-dependency class with a bare RECORD-typed method/constructor parameter hijacks the `Sensors` record detection.** Phase-5 sensor detection walks the (last-discovered) controller's ctor-dependency classes and promotes the *first record found in any method parameter* to "the sensor record" — hijacking the `Sensors` interface and **deleting that record's `datatype`**. *Apply:* type such params as the sealed *interface* (`changeDirection(VehicleEvent cmd)`), and instantiate emission ports inline (`= new SignalPort()`) not via ctor params. **Logged as pipeline gap G-N.**

**F4 — Use the single-field-record constructor pattern for every typed event emission** (`recv.method(new Events.Evt(arg))`) — names the event from the record and types it from the record's field, correct in transition *and* lifted-entry contexts; a bare single-arg call in an entry context synthesises a junk `<evt>_Type_value_int`. (Confirms run-4/6 F2.)

**F5 — Every typed-trigger branch must capture the payload into a matching state var as its FIRST statement** — incl. consume-and-ignore self-loops (e.g. `Found` on `turn`); else the trigger binds to `var v : real` and CSP rejects non-real payloads (pipeline gap G-M).

**F6 — Prefer `p/¬p` (same predicate, negated) over two-enum-literal guards for total cover** — closes the Isabelle disjunction from `St.exhaust_disc` alone (pipeline gap G-L).

**F7 — Spec "entry" actions = identical action duplicated on every non-self incoming transition** (the EGL synthesises `entry` only when all non-self incoming transitions carry byte-identical actions); top-of-mode statements are the other entry source, but multi-arg calls at top-of-mode are NOT routed through the operation-call builder, so `move(lv,a)`-style entries must come via the duplication route (pipeline gap G-F).

**F8 — Distinguish environment verdicts from code verdicts before burning an iter** — exit-127/exe-not-found in dafny/isabelle_verify (foreign `pipeline.yaml` tool paths) look like phase failures but aren't feedback about the Java; fix paths in §2 setup. (Recurring.)

**F9 — The no-Final rerouting composes with inter-controller liveness** — once the emitter (GA) no longer terminates, the receiver's terminal mode (`Found`) must self-loop (consume-and-ignore) on *every* shared event the emitter can still produce (`stop`/`turn`/`resume`), or the synchronous connections block. The reroute is a *pair* of changes. (Confirms cd run-5-rerun F5 / run-8 F4.)

---

## 11. Requirement-notation → Java-realization cross-reference (chemical_detector)

**Why this section exists.** The leakage sweep (June 2026) found chemical_detector's
`requirement_all.json` carries ~29 requirements written in RoboChart/CSP *target
notation* (`Seq(...)`, enum `::`, event `?`/`!`, `since(T)`, final-junction `j1`,
per-transition `Trigger:/Guard:/Action:` labels). This cross-reference checks what
that notation *actually became* in the generated Java across the runs.
**Headline finding (X0): not one Tier-A construct was implemented literally — every
one had to be *un-mapped* to a Java idiom, and three were provably or practically
unimplementable as written.** So the notation is not just leakage that pre-answers
the design; it is leakage the codegen must first *undo*. The "behavioural intent"
column is the input to the requirement remediation (the rewrite that says WHAT, not
the RoboChart HOW).

Evidence base: `run-8/iter-1` (clean cold-converged; lowercase spec-named events)
read in full; spread confirmed by recon across cd runs 2–9. `file:element` refers to
`experiments/convergence/chemical_detector/run-8/iter-1/java/`.

| # | Requirement notation (reqs) | Java realization (evidence) | Spread across runs | Behavioural intent (what the req should say) |
|---|---|---|---|---|
| **X1** | `Seq(GasSensor)` (CD-DM6, CD-DM7, CD-Evt1, CD-GA-Var1) | `record gas(List<GasSensor> value)`; field `List<GasSensor> gs = List.of()` (`event/InputEvent.java`, `controller/GasAnalysis.java`) | **all current runs** use `List<GasSensor>` (payload component named `value`/`reading`/`payload`); historical `b170a79` made `gas` a *signal* + staged the reading on a Sensor cache to shrink FDR4 | "the gas event carries a multi-sensor reading — an ordered collection of (chemical, intensity) pairs" |
| **X2** | enum `::` — `Status::noGas`, `Angle::Front` (CD-OP1, CD-GA-FR4, CD-MV-FR3, CD-GA-Beh4, CD-GA-Beh5) | Java enum constant via `==` / direct ref: `sts == Status.noGas`, `vehicle.move(0.0, Angle.Front)` | uniform | "when the analysis result is *noGas*"; "move facing *Front*" |
| **X3** | event receive `gas ? gs`, `turn ? a`, `obstacle ? l` (CD-GA-Beh2; CD-MV-Beh2/5/7/10/13/16/21; CD-MV-Var1/3/4; CD-MV-FR4/5) | `instanceof` + cast + **payload capture as first branch statement**: `if (event instanceof InputEvent.gas){ var g=(InputEvent.gas)event; gs=g.value(); … }` | uniform (capture-first-statement is load-bearing — gap G-M) | "on receiving the *turn* event, adopt the supplied direction" |
| **X4** | event send `send turn ! anl`, `send resume` (CD-GA-Beh4/6/7) | `actuator.apply(new OutputEvent.turn(anl))`, `actuator.apply(new OutputEvent.resume())` | uniform (single-field-record ctor pattern — F4) | "emit the *turn* event carrying the chosen direction" |
| **X5** | `since(T)` clock predicate (CD-MV-Clock1, CD-MV-FR6, CD-MV-Beh17, CD-MV-Beh18) | `Clock` dependency (`nowMs()`) + `long t` field assigned `t = timer.nowMs()`; guard `timer.nowMs() - t < ChemConstants.stuckPeriod` (`sensor/Clock.java`, `controller/Movement.java`) | uniform; clock field named `t`/`tReset`, dep `timer`/`cycleClock` | "if less than *stuckPeriod* has elapsed since the first obstacle of this evasion" |
| **X6** | **final state `j1`** (CD-GA-FR4, CD-GA-Beh6, CD-MV-FR3, CD-MV-Beh9) | **NO RoboChart Final node** — rerouted to a *live absorbing* mode (`GaMode.Done`, `MvMode.Found`) with an event self-loop (`Done` on `gas`; `Found` on `stop`); terminating event (`stop`/`flag`) emitted on entry | uniform in *outcome* (live sink); naming varies `Done`/`Found`/`Final`(+Tick self-loop)/`Stopped`/`Halted` | "on success the system emits the signal and **halts** (remains stopped)" — drop "final state j1"; it is **unprovable** as written (deadlock_free) |
| **X7** | op signatures `move(lv : real, a : Angle)`, `changeDirection(l : Loc)` (CD-OP1, CD-OP4) | plain methods `Vehicle.move(double lv, Angle a)`, `changeDirection(Loc l)` (`actuator/Vehicle.java`) | uniform | "move at linear velocity *lv* in direction *a*"; "steer away from an obstacle on side *l*" |
| **X8** | **event-in-action `odometer ? d0` / `? d1`** (CD-Evt3, CD-MV-Var2/3, CD-MV-FR4, CD-MV-Beh16) | **sensor read, not an event**: `d0 = telemetry.odometer()`; `odometer` is a `@SensorService` method, never an `InputEvent` variant | uniform | "record the current cumulative distance travelled" — an action-position input event is inexpressible in the single-method `step()` pattern |
| **X9** | `goreq(ins, thr)` as the threshold function (CD-Fn4, CD-GA-Beh6) | `Telemetry.goreq(double,double)` **exists** but the guard **inlines** `ins >= ChemConstants.thr`; `goreq` is used only internally inside `intensity`/`location` | uniform (E3 / first-param-only inference) | "when the peak intensity is at or above the threshold" — describe the comparison, don't mandate a binary guard function |
| **X10** | 2-value enum total cover `sts == noGas` / `sts == gasD` (CD-GA-FR3, CD-GA-Beh4/5) | propositional `if (stsIsNoGas) … else if (!stsIsNoGas) …` (NOT two enum-literal branches) | uniform (F6 — `p/¬p` closes Isabelle from `St.exhaust_disc`) | "classification yields exactly two outcomes: keep searching, or a target is present" |
| **X11** | `during` action `randomWalk()` (CD-MV-FR1) | top-of-mode statement `vehicle.randomWalk();` at the head of the `Waiting` block (lifted to entry) | uniform | "while waiting between readings, the robot performs random-walk search" |
| **X12** | per-transition `Trigger:/Guard:/Action:` labels (CD-GA-Beh1–7, CD-MV-Beh1–23) | the mode-nested if-else branch table — the labels *are* the transition list | uniform | describe each behavioural transition in prose ("in *Going*, on *obstacle*, begin avoiding"); drop the RoboChart labels |

**F-X1 — Tier-A notation is anti-leakage: it costs the codegen an *un-mapping* step
rather than helping.** Every construct above was translated *away from* its RoboChart
form (X1–X12). Three were not implementable as literally specified and forced a
documented deviation in *every* run: the `j1` final state (X6 — always a live sink,
else `deadlock_free` is unprovable), the `odometer ?` action-input (X8 — always a
sensor read), and the `Seq` channel payload (X1 — tractable only under `{0..1}`, or
reduced to signal+cache in `b170a79`). A behavioural spec would have been strictly
*easier* to implement than the model-notation spec.

**F-X2 — Tier-A *notation* was the leak (now removed); the state-machine *prose* is
specification altitude, not leakage.** The Tier-A constructs `Seq(GasSensor)`,
`Status::`, `move(lv:real,…)` handed the agent the event-payload types, enum
identities, and operation signatures **in the target formalism's own syntax** — a
genuine leak, now stripped (commit `3655de46`). The *remaining* state/transition/
variable prose (CD-GA-FR2: "Reading is the initial state… on a gas reading it moves to
Analysis") is **not** leakage: naming modes and describing transitions in plain English
is how a human would specify a reactive controller to a vibe-coding agent, and is about
as specific as natural language gets. The real cross-study difference is therefore one
of **specification altitude** — chemical_detector is specified at a *lower (more
design-committed)* altitude, while LRE/SRanger give behaviour + units
(`reqVel carries a real velocity in m/s`) and leave the control structure open. Treat
this as a property of the inputs when interpreting iteration counts (cd runs exercise
less open design and more rendering), **not** as cd "cheating" or "transliteration".
(Distinct from the §10.5 caveat, which is about study-specific guidance carried in
`CLAUDE.md`.)

**F-X3 — the realizations are consistent enough to invert into a spec.** Across cd
runs 2–9 the mappings X1–X12 are uniform (only payload-component and terminal-mode
naming vary), so the "behavioural intent" column is a reliable basis for rewriting the
requirements **without changing what any converged run actually built**. That column
is the source for the chemical_detector remediation list (companion to this section).
