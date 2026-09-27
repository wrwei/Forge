Drafted by me from lre_requirement_all.txt (SHA-256 above) alone; I did not consult the FORGE repository, any implementation artefact, or any prior spec draft, and the interpretive decisions are my own.

Source SHA-256: `aa9a0e2a27b8043e645353d20053789202922432c32ee2024e605e4edf5f149b`

This is a requirements-level mode-transition reference, drafted on 2026-09-08. The digest was verified before the source was read. The source contains 51 distinct LRE entries, declares exactly four operating modes, and contains no SB-prefixed identifiers or five-mode declaration. Only the supplied requirements file was used as evidence. The output was created in a new directory outside the workspace without inspecting any repository or existing draft. Exposure disclosure: no prohibited material was present in the conversation context available to me; I cannot audit exposure in conversations outside that context.

**Scope.** The relation below specifies mode changes, operator input handling, and the output events associated with those changes. It is a relation over one freshly evaluated sensor/derived-data snapshot per step. It does not define vehicle dynamics or invent a numerical CPA algorithm or evasive manoeuvre. Statements labelled A01–A17 are interpretive decisions, not additional claims about what the requirements explicitly say.

**State and observations.** The operating-mode set is exactly

`Mode = {OCM, MOM, HCM, CAM}`.

At power-up the mode becomes OCM. There is no fifth initialization mode. Optional actuator-state variables `lastVel` and `lastHdng` record the most recently emitted values; before their first corresponding output, their values are unspecified. The mode relation does not depend on those stored values. [LRE-DM1, DM8, FR1, Beh1; A09]

An ordinary step observes one input

`e ∈ {none, reqVel(v), reqHdng(h), reqOCM, reqMOM, reqHCM, endTask}`,

where `v` and `h` are real values. `none` denotes a step without an operator input; it is not an interface event. A step can still take an autonomous transition when its input has no handler in the current mode. [LRE-ARCH2, DM6; A01, A03]

The same sensor snapshot is used for all derived quantities and guards in a step. Write:

| Symbol | Meaning |
|---|---|
| `s` | `minSafeDist` |
| `H` | `staticObsHorizDist` |
| `V` | `staticObsVertDist` |
| `D` | `staticObsDfltVertDist` |
| `ds` | `odist(cstc)` |
| `dd` | `odist(cdyn)` |
| `hs` | `hdist(cstc)` |
| `vs` | `vdist(cstc)` |
| `P` | `inOpez` |
| `C` | `cda < s ∧ tcpa ≥ 0` |

All four thresholds default to 1. They remain distinct parameters. Literal `1` values in the transition table are not replaced by one of these parameters. [LRE-DM4; A12]

For an existing obstacle `i`, evaluated at the current AUV depth:

`hdist(i) = sqrt(ns_rel_dist(i)² + ew_rel_dist(i)²)`

`vdist(i) = abs(depth − obs_depth(i))`

`odist(i) = sqrt(hdist(i)² + vdist(i)²)`.

An obstacle is static exactly when both its horizontal velocity components are zero; its rate of climb does not affect this classification. `cstc` and `cdyn` select the static and dynamic obstacles, respectively, with minimum **overall** distance. For equal minimum distances, choose the smallest identifier. Use `−1` when the corresponding class is empty. Thus both index variables have domain `ℕ ∪ {−1}`. [LRE-DM2, DM3, DM5, SF1–SF3, OP3, OP4, Var5, Var6; A05, A06]

Before evaluating any transition guard, establish:

`hvel = sqrt(ns_vel² + ew_vel²)`

`vvel = rate_of_climb`

`vel = sqrt(hvel² + vvel²)`

`P = (ds ≤ s) ∨ (depth ≤ 0)`.

`CalcCStc` precedes `CheckOPEZ`; `CalcCDyn` precedes `CalcCPA`. All derived values used in one step come from that step's snapshot. Advice emitted during the step does not replace measured velocity or cause guards to be reevaluated within that step. [LRE-OP1–OP5, Var1–Var8, GP1; A01]

For the no-obstacle defaults, let `F` be a fixed real value satisfying

`F > max(0, 1, s, H, V, D)`.

At index `−1`, take `odist = hdist = vdist = F` and the four obstacle field accessors listed in DM5 to be zero. These sentinel distances do not obey the existing-obstacle geometry equations above. When `cdyn = −1`, take `cda = F` and `tcpa = 0`. In particular, absence of dynamic obstacles cannot enable `C` and does enable `cda ≥ s`. Absence of static obstacles makes the obstacle part of `P` false, but does not disable the `depth ≤ 0` part. [LRE-DM5, OP2, OP5; A07]

When a dynamic obstacle exists, `cda ≥ 0` and `tcpa ∈ ℝ` are supplied as the freshly evaluated results of `CalcCPA`. The transition relation accepts them as observations. No formula relating them to raw position and velocity is imposed here: OP5 does not specify one. This leaves the mode relation defined for every admissible observation, while deliberately not defining a complete raw-sensor-to-command function. [LRE-OP5, Var7, Var8; A08]

**How to read the relation.** In the table, an input trigger requires that exact current input. An `autonomous` trigger requires no operator event and remains eligible whatever `e` is. Each guard is evaluated in the source mode using the current snapshot.

For an ordinary step, let `E(m,e,x)` be the set of rows T02–T18 whose source is `m`, whose input trigger, if any, matches `e`, and whose guard is true in snapshot `x`. If this set is nonempty, choose exactly one row in it, change to its destination, and emit exactly its listed output sequence. If the set is empty, retain the mode and emit `[]`. The current input is consumed in either case. The snapshot `x` is an observation, not an internal value preserved by this transition. There is no priority among enabled rows and no optional stuttering when a row is enabled. [A01–A04]

Equivalently, the mode/output relation is:

`R(m,e,x) = {(destination(t), outputs(t)) : t ∈ E(m,e,x)}` when `E(m,e,x) ≠ ∅`;

`R(m,e,x) = {(m, [])}` otherwise.

Each listed output updates its corresponding actuator record, and the other record retains its previous value. `[]` means no output event: it does not mean a zero command or a reset. The table includes destination-entry advice, so that advice must not be emitted a second time. Startup is the separate T01 initialization rule. [LRE-DM7, DM8, FR2, FR3; A09, A10]

| Row / source requirement | Source | Trigger | Guard | Destination | Output sequence |
|---|---|---|---|---|---|
| T01 / LRE-Beh1 | Power-up, outside the operating-mode relation | Power-up | `true` | OCM | `[]` |
| T02 / LRE-Beh2 | OCM | `reqVel(v)` | `true` | OCM | `[advVel(v)]` |
| T03 / LRE-Beh3 | OCM | `reqHdng(h)` | `true` | OCM | `[advHdng(h)]` |
| T04 / LRE-Beh4 | OCM | `reqMOM` | `vel ≤ 1 ∧ ¬P ∧ dd > 1 ∧ ds > 1` | MOM | `[advVel(1)]` on entry |
| T05 / LRE-Beh5 | MOM | autonomous | `P` | OCM | `[]` |
| T06 / LRE-Beh6 | MOM | `reqOCM` | `true` | OCM | `[]` |
| T07 / LRE-Beh7 | MOM | `endTask` | `true` | OCM | `[advVel(0)]` |
| T08 / LRE-Beh8 | MOM | autonomous | `cda < s ∧ tcpa ≥ 0` | CAM | `[]` under A10 |
| T09 / LRE-Beh9 | MOM | autonomous | `hvel ≥ 1 ∧ hs ≤ H` | HCM | `[advVel(0)]` on entry |
| T10 / LRE-Beh10 | MOM | autonomous | `vs ≤ D` | HCM | `[advVel(0)]` on entry |
| T11 / LRE-Beh11 | MOM | autonomous | `vvel ≥ 1 ∧ vs ≤ V` | HCM | `[advVel(0)]` on entry |
| T12 / LRE-Beh12 | MOM | `reqHCM` | `true` | HCM | `[advVel(0)]` on entry |
| T13 / LRE-Beh13 | HCM | autonomous | `hs > H ∧ vs > V` | MOM | `[advVel(1)]` on entry |
| T14 / LRE-Beh14 | HCM | autonomous | `cda < s ∧ tcpa ≥ 0` | CAM | `[]` under A10 |
| T15 / LRE-Beh15 | HCM | `reqOCM` | `true` | OCM | `[]` |
| T16 / LRE-Beh16 | HCM | autonomous | `P` | OCM | `[]` |
| T17 / LRE-Beh17 | CAM | `reqOCM` | `true` | OCM | `[]` |
| T18 / LRE-Beh18 | CAM | autonomous | `cda ≥ s` | OCM | `[advVel(0)]` |

The three autonomous MOM-to-HCM conditions are alternatives, not a conjunction. T10 has no velocity condition. T13 uses `V`, not `D`, and has neither an OPEZ veto nor a CPA veto; those would be new guards. T18 has no `tcpa` condition. There is no autonomous OCM exit, CAM-to-MOM or CAM-to-HCM transition, or CAM exit solely because OPEZ holds. [LRE-Beh4, Beh9–Beh14, Beh17, Beh18; A11]

**Input handling where no explicit row applies.** This table describes only input-specific handling. An autonomous row can still be taken during that step.

| Current mode | Inputs with explicit handlers | Inputs consumed without an input-specific action |
|---|---|---|
| OCM | `reqVel`, `reqHdng`, guarded `reqMOM` | `reqOCM`, `reqHCM`, `endTask`; also `reqMOM` when T04's guard is false |
| MOM | `reqOCM`, `reqHCM`, `endTask` | `reqVel`, `reqHdng`, `reqMOM` |
| HCM | `reqOCM` | `reqVel`, `reqHdng`, `reqMOM`, `reqHCM`, `endTask` |
| CAM | `reqOCM` | `reqVel`, `reqHdng`, `reqMOM`, `reqHCM`, `endTask` |

OCM passes velocity and heading payloads unchanged, without range checks, clipping, normalization, or unit conversion. In other modes, those requests are neither forwarded nor remembered for later forwarding. A request arriving during a transition into OCM is handled according to the source mode; it is not replayed after entry. [LRE-Beh2, Beh3, Beh19; A03, A14]

**Ambiguity ledger.** These decisions complete the transition relation. Their effects should remain distinguishable from direct requirements when using the reference as an oracle.

| ID | Underdetermined point or tension | Resolution used in this reference |
|---|---|---|
| A01 | ARCH2 says guards are evaluated each step, but gives no timing model, event/sensor order, or rule for cascading transitions. | Use an atomic step with one coherent current sensor snapshot, fresh derived quantities, and at most one selected transition. Evaluate input-triggered and autonomous guards together against the source mode. Autonomous conditions are level-triggered, not rising-edge-triggered. Entry outputs do not cause another transition within that step. |
| A02 | Several transitions can be enabled together; the file assigns no priority, including between operator commands, OPEZ, collision avoidance, and caution. | Admit every enabled row as a possible alternative, selecting exactly one. Requirement numbering and table order confer no priority. No extra veto is inferred. This is an explicitly nondeterministic resolution; it does not require a fixed safety or operator priority. |
| A03 | There is no event-queue, retry, or blocked-request policy. Beh19 explicitly excludes some input handling but does not give a full rule for all modes. | Consume every presented input in its current step. Unhandled inputs and failed guarded requests have no input-specific effect. If an autonomous row is selected instead of an enabled input row, the input is still consumed. No buffering, deferral, or replay occurs. |
| A04 | The file does not specify simultaneous operator events or whether enabled autonomous transitions may be postponed indefinitely. | Present at most one operator event per step. Simultaneous arrivals can be serialized in any order, with one step per event; the source fixes no order or intervening sensor evolution. With no operator event, steps still occur. Take some enabled row on each step; stutter only if no row is enabled. No fairness or real-time bound beyond this per-step rule is imposed. |
| A05 | Var5 and Var6 call the indices natural numbers while also prescribing `−1`; ties between closest obstacles are not resolved. | Extend each index domain to `ℕ ∪ {−1}`. Among equally close obstacles of the appropriate class, use the smallest natural identifier. The tie rule can affect `hs`, `vs`, or CPA and is an interpretive choice. |
| A06 | DM3 permits a partial function over natural identifiers without stating finiteness; minimum selection can fail for certain infinite collections. | Assume every operational obstacle register is finite. Static classification uses exactly DM2's two horizontal zero tests, even if an obstacle has nonzero vertical velocity. |
| A07 | DM5 prescribes zero relative-position/velocity accessors for missing obstacles, while OP5 refers to large-distance defaults without defining the resulting CPA pair. “Safe large” has no numeric value. | Use the real sentinel `F` defined above for missing distances and `cda`, and choose `tcpa = 0` for no dynamic obstacle. Treat the missing-obstacle case explicitly; do not feed its zero relative-position accessors into an invented CPA formula. This is a chosen safe completion, not a formula stated in OP5. |
| A08 | OP5 does not specify relative-velocity sign, two- versus three-dimensional CPA, treatment of a past CPA, or zero relative speed. | Leave CPA for existing dynamic obstacles abstract at the mode interface: provide a nonnegative real `cda` and real `tcpa` each step. Do not clamp negative `tcpa`, assume a collision cone, or add a numerical formula. A raw-sensor-level checker must separately identify its CPA convention; this reference does not independently validate that convention. |
| A09 | Startup actuator values, OCM entry outputs, exit actions, and repeated in-mode advice are unspecified. | Emit nothing on startup or OCM entry unless a selected row explicitly supplies output. Emit MOM/HCM advice once on each actual entry. Empty-output steps preserve actuator records. No periodic reissue or exit output is added; initial record values remain unspecified. |
| A10 | FR4 requires evasive manoeuvres in CAM but specifies no command, direction, speed, timing, or algorithm. Other general mode descriptions also do not specify ongoing output schedules. | For this mode-transition reference, use a minimal output completion: emit only the outputs in the table, with no additional CAM entry or in-mode output. This does not specify or discharge FR4's physical manoeuvre obligation. A comparison of evasive-command algorithms cannot be supported by this file alone. |
| A11 | Descriptive text such as “potential collision” or “reduced velocity” could invite extra guards, broader exits, or inferred actions absent from the numbered behaviours. | Use the numbered guards literally. In particular, HCM entry advises exactly zero; `endTask` acts only in MOM; negative `tcpa` alone does not release CAM; and OPEZ alone does not release CAM. No additional transitions or safety vetoes are inferred from mode names. |
| A12 | DM4 gives defaults but no valid configuration range or relationship between thresholds. Several guards use literal 1, and some use different static thresholds. | Keep thresholds as finite real parameters with default 1 and no added ordering or positivity precondition. Keep all literal 1 values literal. Require `F` to exceed every relevant threshold. No hysteresis or equivalence between `V` and `D` is inferred. |
| A13 | Equality at thresholds and the sign of vertical velocity are common implementation choices, but the relevant prose here is explicit. | Use exact real comparisons with the strictness shown in the table; add no tolerance. Use signed `vvel = rate_of_climb`, not its absolute value. This records that no unresolved equality decision was needed for these guards. |
| A14 | Payload validity, stale data, sensor failures, and numeric exceptional values are not specified. | Model finite real sensor values and request payloads and coherent snapshots. Forward OCM payloads exactly. NaN, infinities, hardware failures, malformed events, and stale-snapshot recovery are outside this reference's input domain, not newly invented controller behaviours. |
| A15 | DC1 requires one transition per Beh1–Beh18 and forbids duplicate source/trigger combinations, yet several behaviours are autonomous exits from the same mode. | Keep exactly one labelled row per Beh1–Beh18, including initialization and the two pass-through transitions. Read source/event uniqueness as applying to explicitly named input events; retain distinct autonomous requirement rows with their own guards. DC1 supplies no priority or global guard-disjointness rule. If it was intended to forbid all multiple autonomous exits, it conflicts with the listed behaviours. |
| A16 | No sampling frequency, environmental evolution, actuator-to-plant delay, or liveness guarantee is given. Overlapping entry and exit conditions can cause repeated mode changes across steps. | State only the per-step relation. Do not promise eventual stability, eventual OCM, or physical collision avoidance. Permit repeated transitions allowed by unchanged or changing observations; do not insert dwell times, debounce, or stabilization logic. |
| A17 | Different enabled rows can have the same destination but different output obligations. It is unspecified whether their actions should be merged. | Execute only the selected row's actions plus its destination-entry advice, as already combined in the table. Do not merge enabled rows' actions. Preserve row identity when recording a witness; for conformance compare both destination and output sequence. |

**Boundary and overlap consequences.** Each boundary statement concerns the indicated guard; another enabled row can still determine the selected outcome.

| Observation | Consequence |
|---|---|
| `vel = 1` | Satisfies T04's velocity conjunct. |
| `dd = 1` or `ds = 1` | Fails T04's corresponding strict-distance conjunct. |
| `ds = s` or `depth = 0` | Makes `P` true. |
| `cda = s` | Disables T08/T14 and enables T18, regardless of `tcpa`. |
| `cda < s` and `tcpa = 0` | Enables the collision guard `C`. |
| `cda < s` and `tcpa < 0` | Disables `C`; also does not enable T18. CAM therefore remains CAM unless `reqOCM` is selected. |
| `hvel = 1` and `hs = H` | Enables T09. |
| `vs = D` | Enables T10 at any velocity. |
| `vvel = 1` and `vs = V` | Enables T11. |
| `vvel = −1` and `vs ≤ V` | Does not satisfy T11's velocity condition; T10 may independently apply. |
| `hs = H` or `vs = V` | Fails T13. Both distances must strictly exceed their respective thresholds. |

For example, in CAM with input `reqOCM` and `cda ≥ s`, T17 and T18 are both eligible. The allowed outcomes are `(OCM, [])` and `(OCM, [advVel(0)])`. Choosing OCM does not by itself determine whether velocity advice was emitted.

In MOM, if `P` and `C` are true and at least one static-caution guard is true, OCM, CAM, and HCM are all permitted destinations. HCM includes entry output `[advVel(0)]`; the other two alternatives emit nothing. This overlap is intentional under A02.

With no obstacles, positive depth, and no operator input, HCM transitions to MOM; CAM transitions to OCM with `[advVel(0)]`; MOM remains MOM. OCM remains OCM until a guarded `reqMOM` arrives. If depth is nonpositive, absence of obstacles still leaves `P` true; for example, HCM can have both T13 and T16 enabled.

There is no stability guarantee even with constant observations. For example, when `D > V`, observations with `hs > H` and `V < vs ≤ D` enable HCM-to-MOM via T13 and then MOM-to-HCM via T10 on the next step. With no competing guards, the modes alternate and the entry advice alternates between 1 and 0. No repair to this requirements-permitted behaviour is introduced here.

**Use as a reference oracle.** A single-step mode/output observation conforms exactly when its `(destination, output sequence)` belongs to `R(m,e,x)`, starting from the specified OCM initialization. An implementation with a fixed priority can select a subset of the allowed alternatives; this reference does not require it to exercise every alternative. Conversely, a mode change or output absent from the relation is rejected under this reference's recorded decisions. The trace-level interpretation is successive application of the same rule, with no additional fairness requirement.

Priority-only differences may therefore remain undetected by this reference. Findings involving event consumption, obstacle ties, missing-obstacle CPA, or unspecified output behaviour depend on the corresponding ledger choices and should be reported as such. Findings about CPA arithmetic for existing obstacles or the effectiveness of evasive manoeuvres are outside this mode relation's evidential scope. No extracted model, mutation population, verifier result, or measured kill rate was consulted, and no such result is claimed here.
