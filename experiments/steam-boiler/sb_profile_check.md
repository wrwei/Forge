# Steam Boiler vs. the frozen admitted profile

Frozen profile: `review/steamboiler-profile-freeze.json` (paper repo),
frozen 2026-09-02, repo head 26fe49fb. **Hash verification performed
2026-09-03**: all four profile files re-hashed with `shasum -a 256`; every
digest matches the freeze record exactly (StructuralLinter.java 7ab9c729…,
java_codegen_rules.txt 8e53c4f4…, codegen_trace_rules.txt ea6b4eb0…,
CLAUDE.md e485cf30…), and `git rev-parse HEAD` = 26fe49fb0049…, matching
`repo_head`. The assessment below is therefore against the exact frozen
state.

Reference basis for "never exercised": the requirement sets of the three
admitted case studies (lre, sranger, chemical_detector) under
`forge.assets/case-studies/*/requirements/`, inspected read-only.

## A. Entry-by-entry: StructuralLinter rules (rules_enumerated in freeze)

| Rule | What it admits/forbids | Steam-boiler fit |
|------|------------------------|------------------|
| rule0_no_types_found | generated project must contain types | Neutral — trivially satisfiable. |
| rule1_compute_local_variable | Operation.compute() = direct field assignments only, no locals | **Stressed.** Rescue-mode level estimation needs interval bounds (two fields, e.g. lower/upper) advanced from previous-cycle values of v, q, pump inflow. Expressible as chained `this.field = expr` assignments (previously-assigned fields may be referenced), but the boiler is the first study whose compute() carries **state across cycles** (previous readings) rather than being a per-step function of sensor values. Fits the letter of the rule; exercises a case the three studies never did. |
| rule2_step_outer_ne | outer if-else = `currentMode == X` only, one block per mode | Fits — five modes → five outer blocks. Emergency-stop and STOP-message priority branches must be duplicated per mode block (as the codegen rules already require for cross-mode overrides). |
| rule3_step_no_named_predicates | warns when if-conditions contain raw method calls instead of named booleans | Fits — guards (levelBelowN1, levelRisksM1, transmissionFailed, anyUnitDefective, …) are naturally named booleans. **But** several predicates aggregate over the four pumps ("all repaired", "any pump defective"); under the no-inline-logic rule these aggregations must be pushed into Operation/Sensor-layer methods (cf. LRE's closestStaticIndex pattern). First study to need quantification over a unit family. |
| rule4_double_missing_real_annotation | every double needs @RoboChartType("real") | Fits — q, v, p and the bound estimates are reals. Mechanical. |
| rule5_robochart_on_local | no @RoboChartType on locals | Neutral. |
| rule6_missing_priority_negation | inner if-chain branches must conjoin negations of higher-priority guards | **Stressed.** The boiler's per-mode inner chains are long (normal mode alone: STOP-count, transmission failure, level-risk, level-unit failure, other-unit failure, pump switching). Priority-negation obligations grow quadratically with chain length; the boiler's chains are longer than any of the three studies'. Fits, but this is where lint pressure will concentrate. |
| rule7_signed_sentinel_on_controller_state | warns on -1 sentinels in controller state (int→nat unconditional) | Fits — no sentinel needed: pump indices are 1..4, the STOP counter is 0..3. The nat-only int mapping is safe for every boiler quantity. |
| rule8_event_branch_precedes_triggerless | event-triggered branches must precede triggerless ones | **Stressed differently.** The boiler cycle is not single-event-driven: each cycle delivers a batch of messages (level, steam, 4 pump states, 4 controller states, plus optional repair/ack/STOP). If the batch is modelled as one composite input event, almost every transition is "triggered" by the same event and ordering degenerates to guard priority (rule6); if messages are modelled as separate events, the single `step(InputEvent event)` per-invocation shape cannot see a whole cycle at once. Either encoding is outside what lre/sranger/chemical_detector exercised. |

## B. java_codegen_rules.txt (frozen prompt)

- **Single-method mode-nested if-else, enum mode field** — fits: the boiler
  is a mode-dispatching controller with exactly five modes; this is the
  pattern the profile was built for. Verdict: **core fit**.
- **step(InputEvent event) single-event entry** — see rule8 row: the
  five-second batched-simultaneous-message cycle (sect 3 of the chapter) is
  the largest structural mismatch. The admitted studies all consume one
  event per step. The boiler needs either (a) a composite per-cycle input
  record carrying all mandatory readings, or (b) a Sensor-layer facade that
  the step() consults, with the "event" reduced to a cycle tick. Both are
  representable in the profile's vocabulary, but **neither encoding was
  ever exercised**.
- **No collections in controller predicates / sentinel handling in Sensor
  layer** — the four pumps + four controllers demand per-unit status. The
  profile maps List/Set/Collection → Seq(...) (CLAUDE.md type table), and
  chemical_detector exercised a list-valued *event payload* (GasSensorReading),
  but **no study ever kept an indexed unit-status collection as controller
  state** (LRE's ObstacleRegister lives in the Sensor layer). Boiler
  encoding options: four scalar boolean fields per unit class (statically
  unrolled — fits, ugly, 8+ fields), or Seq-typed state (unexercised).
- **Named predicates, simple RHS** — fits with the aggregation caveat above.
- **Parameterised output events** — OPEN_PUMP(n)/CLOSE_PUMP(n) carry a nat;
  sranger's Move(v, a) already exercised multi-arg outputs. Fits.
- **Two-field input payloads** — PUMP_STATE(n, b) / PUMP_CONTROL_STATE(n, b)
  are (nat, boolean) pairs, ×4 per cycle. Record payloads exist in the
  profile (records → own RoboChart types), but a per-cycle *family* of
  same-typed indexed payloads was never exercised.
- **Ban on while-loops/queues/threads** — fits; nothing in the boiler needs
  them (for-loops over the four pumps are allowed where the LRE-style
  Sensor/Operation layer iterates).
- **@RoboChartType nat/real discipline** — fits cleanly (see rule7 row).

## C. codegen_trace_rules.txt

Purely mechanical trace-file contract; requirement `types` used here
(architecture, data_type, event, constants, state, variable, transition,
constraint, operation, actuator) were reused in the SB requirement set, so
the trace step transfers unchanged. Verdict: fits.

## D. CLAUDE.md (frozen pipeline constraints)

- Type map int→nat unconditional: safe for the boiler (no negative
  quantities; gradients U1/U2 are magnitudes with direction fixed by
  context). Fits.
- Clock support: `clock` promotion exists (`clock.nowMs()` pattern,
  `since(...)` rewriting) and sranger/chemical_detector exercised
  single-clock elapsed-time guards. The boiler needs **discrete cycle
  counting** (STOP three cycles in a row; pump check on the following
  transmission; controller check on the second transmission) rather than
  wall-clock elapsed time. Counters-as-nat-state fit the profile; the
  timed-obligation *pattern* (per-pump countdown after an order) was never
  exercised.
- Parallel composition: chemical_detector already produced a multi-machine
  package (movement + gas analysis), so multi-controller layout exists in
  the profile. The boiler *can* be a single controller (one mode variable);
  parallel composition is NOT required. Verdict: fits without needing the
  multi-machine path, though a design might optionally split
  failure-bookkeeping from level control.
- Terminal mode: emergency stop halts the program (chapter sect 4.5). No
  admitted study has a terminal/absorbing state; RoboChart can express an
  absorbing state trivially (no outgoing transitions), but deadlock-freedom
  style checks used for LRE (LRE-V14) would flag it. **Verification-side
  convention (which CSP assertions to run) is not part of the frozen
  profile files, but the paper should note the absorbing-state novelty.**
- Per-cycle unconditional output MODE(m): admitted studies emit outputs
  only as transition entry actions. A message sent on *every* cycle in
  *every* mode has no exercised precedent; encodable as an action on every
  transition (duplication) or a self-loop, both unexercised.
- Repeat-until-acknowledged outputs (failure detections, sect 5): a
  *persistent output obligation spanning cycles* — encodable as
  status-driven per-cycle sends, same mechanism as MODE(m), same
  unexercised status.

## E. Verdict

**Profile fit: fits-with-stress.** The steam boiler IS a mode-dispatching
controller — five modes, guard-driven transitions, Sensor/Operation
layering, nat/real/boolean state — i.e. squarely inside the admitted
profile's intended class, and no frozen rule *forbids* any construct the
boiler needs. But it stresses the profile at eight points the three
admitted case studies never exercised:

1. Batched simultaneous message input per cycle (vs. one event per step).
2. Indexed unit family: four pumps + four controllers → per-unit status
   state (unrolled scalars or Seq-typed controller state).
3. Two-field indexed input payloads PUMP_STATE(n,b) / PUMP_CONTROL_STATE(n,b).
4. Persistent repeat-until-acknowledged output obligations.
5. Unconditional every-cycle output (MODE broadcast).
6. Discrete cycle-count timing (STOP×3; next/second-transmission failure
   checks) rather than wall-clock elapsed-time guards.
7. Stateful (cross-cycle) Operation compute() — interval estimation of the
   level in rescue mode from previous-cycle values.
8. Terminal/absorbing mode (emergency stop halts the program).

Items 1–3 are the load-bearing ones for the generality claim; 4–8 are
pattern-level novelties expressible with existing constructs. Arrays/
collections: needed only for the pump family, avoidable by static
unrolling (4 is a constant); parallel composition: not needed; timers:
cycle counters suffice, wall-clock not needed.
