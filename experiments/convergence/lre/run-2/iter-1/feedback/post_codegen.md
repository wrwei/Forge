# Post-Codegen Review — lre, iter 1 (cold)

**Status:** complete — all 52 requirements implemented and traced in
`result_codegen.json`. 15 Java files under
`java.generated.project/src/main/java/lre/`.

## Review items

1. **design_choice — cstc/cdyn are `int`, not `@RoboChartType("nat")`.**
   LRE-Var5/6 say "natural number" but use -1 as the no-obstacle
   sentinel; a nat cannot hold -1, so the fields (and sensor index
   parameters) stay plain `int`.
2. **design_choice — Beh4's literal `1` thresholds use `minSafeDist`.**
   The odist guards in OCM→MOM use `LreConstants.minSafeDist` (default
   1) rather than a bare literal; `vel <= 1.0` keeps the literal (MOM
   nominal speed has no named constant).
3. **design_choice — CheckOPEZ/CalcCPA read closest-obstacle indices
   from the Sensor directly** (`closestStaticIndex()` /
   `closestDynamicIndex()`), not via CalcCStc/CalcCDyn getters — the
   compute() RHS whitelist allows only sensor calls and own fields.
   Identical values by construction.
4. **invented_default — CalcCPA formula.** Horizontal-plane CPA:
   `tcpa = -(r·v)/(v·v)`, `cda² = hdist(cdyn)² − (r·v)²/(v·v)`. Using
   `hdist` for the separation makes the no-dynamic-obstacle case safe
   (sensor returns 1.0e6). Zero relative velocity ⇒ `tcpa = -1`,
   `cda = hdist`. `cda²` clamped at 0 before sqrt.
5. **invented_default — safe large distance = 1.0e6 m** in Sensor for
   missing obstacles.
6. **ambiguous_requirement — CAM entry.** "Evasive manoeuvres"
   (LRE-FR4) has no defined advVel/advHdng output; CAM is entered with
   no output action.
7. **design_choice — transition priority order invented.** MOM inner
   order: reqOCM, endTask, reqHCM, inOpez→OCM, CAM guard, HCM guards.
   HCM: reqOCM, inOpez, CAM guard, return-to-MOM.
8. **scope_question — no demo main / tests** (no executable scenario in
   the requirements).

**Next step:** run the deterministic pipeline and iterate on feedback.
