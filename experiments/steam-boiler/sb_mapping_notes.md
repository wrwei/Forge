# Steam Boiler — mapping notes

Source: J.-R. Abrial, "Steam-boiler control specification problem" (Dagstuhl
competition text, LNCS 1165, DOI 10.1007/BFb0027252), 6-page PDF artifact
`steam-boiler-problem.pdf`. All numeric values and constant names below were
verified against the PDF pages **by vision** (the mechanically-extracted text
layer has glyph-broken digits and was not used for any number). Every
description in `requirement_all.{txt,json}` is a paraphrase; each entry below
gives its section anchor and at most one short quote (<15 words) for audit.

Anchor-quote convention: quotes are transcribed from the PDF page image;
`[sic]` marks the original's spelling.

## Per-requirement anchors

| ID | Section (page) | Anchor quote |
|----|----------------|--------------|
| SB-V1 | sect 1–2 (p1) | "to control the level of water in a steam-boiler" |
| SB-V2 | sect 4 (p3) | five modes enumerated: initialization, normal, degraded, rescue, emergency stop |
| SB-V3 | sect 3 (p3) | "This cycle takes place each five seconds" |
| SB-V4 | sect 2.1, 4.2 (p2, p4) | "maintain the water level in the steam-boiler between N1 and N2" |
| SB-V5 | sect 2.2, 2.6 (p2–3) | level device "measures the quantity of water q" |
| SB-V6 | sect 2.5, 2.6 (p2–3) | steam device measures "quantity of steam v" |
| SB-V7 | sect 2.6 (p3) | table row: p, throughput of the pumps (litre/sec) — derived |
| SB-V8 | sect 4.4 (p4) | "taking into account the maximum dynamics of the quantity of steam" — derived |
| SB-V9 | sect 4–6 (p3–6) | failure/repair protocol presupposes per-unit status — derived |
| SB-V10 | sect 6 (p5) | STOP "received three times in a row" — derived counter |
| SB-DM1 | sect 2.1, 2.6 (p2–3) | "Below M1 the steam-boiler would be in danger after five seconds" |
| SB-DM2 | sect 2.1, 2.6 (p2–3) | W, U1, U2 with units litre/sec and litre/sec/sec |
| SB-DM3 | sect 2.3, 2.6 (p2–3) | "the pump needs five seconds to start pouring water" |
| SB-DM4 | sect 2.4 (p2) | controller sees whether water circulates or not |
| SB-DM5 | sect 2.2 (p2) | one water-quantity measuring device |
| SB-DM6 | sect 2.5 (p2) | one steam-outcome measuring device |
| SB-DM7 | sect 2.1, 4.1 (p2–3) | valve "for evacuation" used in the initial phase |
| SB-DM8 | sect 5 (p4–5) | "sends, at each cycle, its current mode of operation" |
| SB-DM9 | sect 6 (p5–6) | data messages "must be present during each transmission" |
| SB-DM10 | sect 4 (p3) | mode value ranges over the five listed modes |
| SB-Beh1 | sect 4, 4.1 (p3) | "The program starts in the initialization mode" |
| SB-Beh2 | sect 4.1 (p3) | "waits for the message STEAM-BOILER_WAITING" |
| SB-Beh3 | sect 4.1 (p3) | "checks whether the quantity of steam coming out of the steam-boiler is really zero" |
| SB-Beh4 | sect 4.1 (p3) | if q above N2, valve activated to empty |
| SB-Beh5 | sect 4.1 (p3) | if q below N1, a pump activated to fill |
| SB-Beh6 | sect 4.1 (p3) | level-device failure during init → emergency stop |
| SB-Beh7 | sect 4.1 (p3) | PROGRAM_READY repeated until PHYSICAL_UNITS_READY |
| SB-Beh8 | sect 4.1 (p3–4) | normal if all units OK, degraded if any unit defective |
| SB-Beh9 | sect 4.1 (p4) | transmission failure during init → emergency stop |
| SB-Beh10 | sect 4.2 (p4) | level below N1 or above N2 → switch pumps on/off |
| SB-Beh11 | sect 4.2 (p4) | level-unit failure → rescue |
| SB-Beh12 | sect 4.2 (p4) | any other unit failure → degraded |
| SB-Beh13 | sect 4.2 (p4) | "evaluated on the basis of a maximal behaviour of the physical units" |
| SB-Beh14 | sect 4.2 (p4) | transmission failure → emergency stop |
| SB-Beh15 | sect 4.3 (p4) | "the functionality is the same as in the preceding case" |
| SB-Beh16 | sect 4.3 (p4) | all defective units repaired → back to normal |
| SB-Beh17 | sect 4.3 (p4) | level-unit failure in degraded → rescue |
| SB-Beh18 | sect 4.3 (p4) | level risking M1/M2 or transmission failure → emergency stop |
| SB-Beh19 | sect 4.4 (p4) | level estimated; n litres from pumps = n litres content |
| SB-Beh20 | sect 4.4 (p4) | level unit repaired → degraded or normal |
| SB-Beh21 | sect 4.4 (p4) | steam unit / pump-control failure / level risk → emergency stop |
| SB-Beh22 | sect 4.5 (p4) | "the physical environmente is then responsible" [sic] |
| SB-Beh23 | sect 4.5, 6 (p4–5) | STOP "received three times in a row" |
| SB-Beh24 | sect 5 (p4) | MODE(m) sent at each cycle |
| SB-Beh25 | sect 2.3 (p2) | five-second pump start latency — derived consequence |
| SB-FR1 | sect 7 (p6) | "the pump changes its state spontaneously" |
| SB-FR2 | sect 7 (p6) | flow indication checked at the SECOND transmission after the order |
| SB-FR3 | sect 7 (p6) | "out of the valid static limits" (0..C) |
| SB-FR4 | sect 7 (p6) | static limits 0..W; dynamics per U1/U2 |
| SB-FR5 | sect 7 (p6) | "a message whose presence is aberrant" |
| SB-FR6 | sect 5 (p5) | detection messages repeated until acknowledgement |
| SB-FR7 | sect 5–6 (p5–6) | repair messages repeated until program acknowledgement |
| SB-FR8 | sect 4.2–4.4 (p4) | severity ordering — derived from separately stated rules |

## AMBIGUITY LOG

Abrial's text is deliberately underdetermined in places (it was set as a
competition problem). Each item: the gap, my resolution as encoded in the
requirements, and whether the resolution is safety-conservative.

1. **"Risk of reaching M1/M2" is not operationalised** (sect 4.2). The text
   says the risk is "evaluated on the basis of a maximal behaviour of the
   physical units" but gives no formula. Resolution: SB-Beh13/18/21 state the
   worst-case criterion abstractly; SB-V8 requires lower/upper level bounds
   advanced from W, U1, U2, P per cycle, so "risk" = the worst-case one-cycle
   extrapolation crossing M1 or M2. **Safety-conservative** (over-approximation
   can only trigger emergency stop early, never late).

2. **Pump throughput p has no reporting device** (sect 2.6 lists p among
   current measures). Resolution: SB-V7 (kind derived) computes p from pump
   states, controller states, and nominal capacity P. **Conservative caveat**:
   in worst-case bounds, a pump ordered on but not yet confirmed pouring
   contributes P to the upper bound and 0 to the lower bound.

3. **Rescue-mode trigger "failure of the pump control units"** (sect 4.4) is
   plural and unquantified — one controller or all four? Resolution:
   SB-Beh21 reads it as ANY pump-controller failure while in rescue forces
   emergency stop, because level estimation relies on knowing pump inflow.
   **Safety-conservative** (stops earlier than a lenient reading).

4. **Simultaneous failure detections are not ordered** (sects 4.2–4.4 state
   transition rules independently). Resolution: SB-FR8 (derived) imposes
   severity precedence emergency > rescue > degraded. **Safety-conservative**.

5. **STOP counting semantics** (sect 6): "three times in a row" does not say
   whether the count survives a mode change or what resets it. Resolution:
   SB-V10 — increment per cycle containing STOP, reset on a STOP-less cycle;
   applies in every mode (sect 4.5 says stop "can also be set directly from
   outside"). **Neutral** (both readings reach emergency stop within three
   cycles of a sustained request).

6. **Init pump use: "a pump"** (sect 4.1) — singular; how many pumps to use
   for initial fill is open. Resolution: SB-Beh5 keeps the singular, leaving
   multiplicity to design. **Neutral** (fill rate affects start-up time, not
   safety; M1/M2 monitoring is not yet active during init but the level check
   bounds remain N1/N2).

7. **Valve closure is implicit** (sect 4.1 empties "the steam-boiler"; sect 5
   says VALVE requests "opening and then closure"). The prose never states
   the closure condition. Resolution: SB-DM8 describes VALVE as
   open-then-close within initialization; closure when the level re-enters
   [N1,N2] follows from SB-Beh7's precondition. Logged as underdetermined;
   entry stays descriptive. **Neutral**.

8. **Degraded-mode assumption vs. transition** (sect 4.3): the mode "assumes
   the level device works correctly", yet also has a rule for level-device
   failure (→ rescue). These coexist: the assumption is about regulation
   validity, not an invariant. Resolution: SB-Beh15 states the assumption,
   SB-Beh17 the transition. **Neutral** (both readings kept).

9. **Failure-detection identity of "second transmission"** (sect 7, pump
   controller): whether "second" counts the transmission cycle of the order
   as zero or one. Resolution: SB-FR2 ties the extra cycle to the pump's
   five-second start latency (sect 2.3), i.e. the flow check happens one
   full cycle after the pump-state check. **Neutral**; flagged for the
   generation step to encode consistently.

10. **STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT vs STEAM_FAILURE_DETECTION**
    (sects 5–6): the receive-side acknowledgement name carries an extra
    OUTCOME token relative to the send-side detection name. Resolution:
    SB-DM9 preserves the asymmetric names verbatim as message vocabulary
    facts. **Neutral** (naming only).

11. **Which failures block initialization** (sect 4.1 tail, p4): the text
    demands emergency stop for level-device failure and transmission failure
    during init, and routes "any physical unit defective" to degraded on
    exit — but does not say whether a STEAM-device failure detected during
    init (other than the v≠0 check of SB-Beh3) also forces emergency stop.
    Resolution: SB-Beh3 covers the explicit v≠0 case; other steam-device
    failures during init fall under SB-Beh8's "some unit defective →
    degraded". Logged because a stricter reading (steam failure during init →
    emergency stop, since init cannot verify the zero-steam condition with a
    dead sensor) is defensible. **Chosen reading is the literal one; the
    stricter alternative noted for the generation step.**

12. **Emergency-stop entry from initialization on level risk**: sect 4.1
    gives no M1/M2 rule during init (regulation targets N1/N2 with the plant
    cold). Resolution: no SB requirement imposes M1/M2 monitoring during
    initialization. **Potentially non-conservative — logged**: a reviewer may
    argue M1/M2 vigilance should be permanent; the chapter's own text scopes
    the risk rules to normal/degraded/rescue (sects 4.2–4.4).

Count: 12 logged ambiguities; 6 requirement entries carry kind
"Derived Requirement" (SB-V7, SB-V8, SB-V9, SB-V10, SB-Beh25, SB-FR8).

## Copyright note
No sentence of the chapter is reproduced. Quotes above are ≤14 words each,
one per requirement at most, used solely as location anchors. Symbolic
constants (C, M1, M2, N1, N2, W, U1, U2, P), units, mode names and message
names are used as uncopyrightable facts.
