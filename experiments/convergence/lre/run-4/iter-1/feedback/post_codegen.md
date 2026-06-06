# Phase 2 — Interactive Code Generation (iter 1, cold)

**Status:** complete

Cold codegen of the LRE controller from `requirement_all.json`: 15 Java files
(`annotation`, `mode`, `constants`, `event`, `sensor`, `actuator`, `operation`,
`controller`). All 51 requirement IDs traced in `result_codegen.json`.

## Items for user review

1. **design_choice — cstc/cdyn typed as plain `int`, not `@RoboChartType("nat")`.**
   LRE-Var5/Var6 call them natural numbers, but the `-1` "no obstacle" sentinel
   is not a nat. Declared as signed int (default RoboChart int mapping), matching
   the hand-maintained `csp_overrides.csp` `core_int = {-1..1}`.

2. **invented_default — Sensor safe large distance = 1.0e6.** LRE-DM5 mandates a
   "safe large distance" when no obstacle exists but gives no value. Used
   `1000000.0`, comfortably above every threshold (all 1.0).

3. **ambiguous_requirement — CalcCPA velocity frame and plane.** LRE-OP5 says
   CalcCPA "accesses the obstacle's relative position and velocity". The
   `obs_ns_vel`/`obs_ew_vel` fields are treated as already-relative velocities;
   CPA is computed in the 2-D horizontal plane (`obs_depth`/`obs_roc` unused).
   `tcpa = -(r·v)/|v|²`, `cda = |r + v·tcpa|`.

4. **ambiguous_requirement — CalcCPA division by zero when no dynamic obstacle.**
   Sensor returns zero-velocity defaults (per LRE-OP5), so `closingSpeedSq = 0`
   and tcpa/cda are NaN in Java. NaN makes `cdaBelowMinSafe`/`tcpaNonNegative`
   false (no spurious CAM entry) and `!cdaBelowMinSafe` true (CAM exits to OCM)
   — safe behaviour, but revisit if Dafny flags the division.

5. **design_choice — velocity thresholds are literal `1.0`.** LRE-Beh4/9/11
   compare vel/hvel/vvel against 1 m/s; `LreConstants` defines only distance
   thresholds. Distance guards in LRE-Beh4 ("greater than 1") use
   `MIN_SAFE_DIST` (default 1).

6. **ambiguous_requirement — CAM has no entry action.** LRE-FR4's "evasive
   manoeuvres" names no concrete advVel/advHdng output and LRE-DM7 limits
   outputs to those two events. No entry action emitted for CAM.

7. **design_choice — inner-branch priority and payload fields.** No relative
   priority is specified among MOM's autonomous transitions; ordered as operator
   overrides > inOpez > CAM > HCM guards (Beh9, Beh10, Beh11). OCM pass-through
   captures reqVel/reqHdng payloads into controller fields `opVel`/`opHdng`
   (typed-trigger pattern); these two fields are not in the LRE-Var list.

**Next step:** run the deterministic pipeline (compile → vacuity) and iterate
from `post_*` feedback.
