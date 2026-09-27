# Steam Boiler — encoding choices for the 8 stressed constructs

Generated 2026-09-03 against the frozen profile (review/steamboiler-profile-freeze.json;
all four profile files verified unchanged). Construct numbering follows
sb_profile_check.md section E.

## 1. Batched simultaneous message input per cycle (SB-V3, SB-DM9)

**Choice: Sensor-layer batch facade + cycle-tick step event** (option (b) of the
profile check). The whole incoming transmission is one record,
`sensor.TransmissionData` (presence flags on the four mandatory data messages,
SB-FR5), ingested by `Sensor.ingest(...)` BEFORE the controller runs. The
controller's `step(InputEvent event)` keeps the profile's single-event entry:
the sole input event variant is `InputEvent.Transmission` — a per-cycle tick.
All guard information reaches step() through Sensor queries, mirroring how the
three admitted studies route sensor state around the event. Consequence for the
extracted model: transitions are autonomous (guard-only) rather than
event-triggered; branch priority is carried entirely by the rule6 negation
discipline. Serves: SB-V3, SB-DM9, SB-FR5.

## 2. Indexed unit family: 4 pumps + 4 pump controllers (SB-V9, SB-DM3, SB-DM4)

**Choice: static unrolling at the controller boundary; arrays confined to the
Sensor/Operation layer.** Controller state carries NO per-unit collection —
aggregate named predicates (`anyControlUnitBroken`, `anyNonLevelUnitBroken`,
`allUnitsSound`, `everyUnitSound`) are computed by the Sensor/StatusRegister
layer (the LRE `closestStaticIndex` pattern: quantification pushed below the
controller). Inside `StatusRegister`/`Sensor`, fixed-length arrays indexed
1..4 with imperative for-loops (allowed outside compute()/step()). Pump
actuation is statically unrolled: `OpenPump(1)..OpenPump(4)` as four explicit
action statements. Seq-typed controller state — the unexercised alternative —
is avoided entirely. Serves: SB-V9, SB-DM3, SB-DM4, SB-Beh10.

## 3. Two-field indexed input payloads PUMP_STATE(n,b) / PUMP_CONTROL_STATE(n,b) (SB-DM9)

**Choice: per-pump record `PumpReport` with four named fields in the batch
record** (`pump1..pump4` in `TransmissionData`) rather than a Seq of pairs.
Records map to their own RoboChart types under the CLAUDE.md type table; the
family is unrolled by name, consistent with choice 2. Serves: SB-DM9, SB-FR1,
SB-FR2.

## 4. Persistent repeat-until-acknowledged outputs (SB-FR6, SB-FR7, SB-DM8)

**Choice: status-driven per-cycle re-emission from the boundary layer.**
`StatusRegister` keeps `*DetectionPending` flags (set at detection, cleared by
the matching acknowledgement) and `*RepairAckDue` flags; `Actuator.
announcePending(register)` re-emits every pending detection and owed repair
acknowledgement each cycle, invoked from `SteamBoilerApp.cycle(...)` after
step(). The obligation lives outside the controller state machine, keeping the
controller's step() within the admitted two-level shape. Serves: SB-FR6,
SB-FR7, SB-DM8.

## 5. Unconditional every-cycle MODE(m) output (SB-Beh24)

**Choice: mode-block entry emission + inline emission on emergency-stop
entry.** Each of the four live mode blocks emits
`ModeMessage(currentMode)` as its FIRST action (duplication across blocks —
the same mechanism the rules already prescribe for cross-mode overrides);
every transition INTO emergency stop additionally emits
`ModeMessage(EMERGENCY_STOP)` inline as its entry action, so the final mode
announcement of a halting run is sent in the cycle that halts. The terminal
mode block itself emits nothing (see 8). Serves: SB-Beh24, SB-DM8.

## 6. Discrete cycle-count timing (SB-V10, SB-Beh23, SB-FR1, SB-FR2, SB-Beh25)

**Choice: nat counters in the Sensor layer; no wall clock.** The STOP streak
is `Sensor.stopCount` (increment on STOP-carrying cycle, reset otherwise,
SB-V10); the pump order windows are per-pump `sinceOrder[n]` counters valued
0/1/2 — 1 = first transmission after an order (pump-state check, SB-FR1),
2 = second transmission (flow check, SB-FR2, reflecting the one-cycle pump
start latency, SB-Beh25/SB-DM3). The clock-promotion path (`@Clock`,
`since(...)`) is deliberately NOT used: nothing here is wall-clock elapsed
time. Serves: SB-V10, SB-Beh23, SB-FR1, SB-FR2, SB-Beh25.

## 7. Stateful (cross-cycle) Operation compute() (SB-V8, SB-Beh19)

**Choice: rule1-conformant chained field assignments with carried fields.**
`CalcLevelEstimate.compute()` is exactly five `this.field = expression;`
statements, no locals: `levelLow/levelHigh` re-anchor on the accepted reading
via `sensor.baseLevelLow(this.projLow)` (passing the PREVIOUS cycle's
projection as the carry-over), then `projLow/projHigh` advance one worst-case
cycle (max steam out / min pump in and vice versa), then the two risk
booleans compare projections against M1/M2. Cross-cycle state is exactly the
persistence of `projLow/projHigh` between compute() calls — fields, as rule1
requires. Min/max clamping lives in Sensor methods (`baseLevelLow`,
`maxSteamOutPerCycle`, ...) so no ternaries or locals appear in compute().
Serves: SB-V8, SB-Beh13, SB-Beh19, SB-Beh25.

## 8. Terminal/absorbing emergency-stop mode (SB-Beh22)

**Choice: empty terminal mode block — deadlock reported as EXPECTED-FAITHFUL,
per the pre-registration.** The EMERGENCY_STOP outer block contains no
transitions and emits nothing; on entering it the program's control task ends
and `SteamBoilerApp.cycle()` refuses further cycles. The prereg's alternative
(a self-loop) is admissible ONLY if it emits no output; but a mode block that
emits the MODE broadcast (choice 5) would violate that condition, and a
no-op self-loop would be a τ-transition competing with nothing — adding no
value while still being a deviation from the halting semantics. The M2M's
advisory deadlock-lint line
(`BoilerController.EMERGENCY_STOP: no outgoing transitions`) fired exactly as
pre-registered; it is recorded as a RESULT (fidelity evidence), not repaired.
Serves: SB-Beh22, SB-Beh23.

## Cross-cutting note: MODE(m) vs the terminal mode

SB-Beh24 says MODE(m) is sent on every cycle "in whatever mode"; SB-Beh22 says
the program stops in emergency stop. These are reconciled by reading SB-Beh22
as ending the cycle rhythm itself (sect 4.5: the program stops; there are no
further cycles to broadcast in). The final MODE(emergency_stop) is emitted in
the entering cycle (choice 5). The stricter reading — a forever-broadcasting
absorbing state — would make deadlock-freedom pass vacuously but violates the
prereg condition that a terminal self-loop must emit NO output messages.
