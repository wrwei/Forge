# post_codegen

**status:** uncertain

**summary:** LRE controller generated in five layers (15 Java files; every requirement traced). Nothing is unimplemented. 15 items flag invented defaults, design choices or ambiguities, chiefly: the -1 sentinel replaced by index 0 (nat profile), and a cda that includes vertical separation so that an absent dynamic obstacle does not trigger CAM.

## Issues

### 1. [ambiguous_requirement] '-1 means no obstacle' conflicts with the nat type of cstc/cdyn

**raw:** LRE-Var5/Var6 declare cstc and cdyn as natural numbers with -1 meaning 'none', and LRE-DM5/OP3/OP4 say closestStaticIndex()/closestDynamicIndex() return -1. The Java profile maps int to nat and forbids negative values or negative 'none' sentinels.

**fix_directive:** Resolved as: ObstacleRegister.NO_OBSTACLE = 0 denotes 'no obstacle'; real obstacle identifiers must be positive (put() rejects 0). Sensor functions given index 0 return the absent-obstacle defaults. Confirm 0 is acceptable as the sentinel.

**java_trace:**

- `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 17-33, `NO_OBSTACLE`: LRE-DM3, LRE-Var5, LRE-Var6
- `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 120-128, `closestStaticIndex`: LRE-DM5, LRE-OP3, LRE-OP4
- `java.generated.project/src/main/java/lre/operation/CalcCStc.java` lines 13-14, `cstc`: LRE-Var5
- `java.generated.project/src/main/java/lre/operation/CalcCDyn.java` lines 13-14, `cdyn`: LRE-Var6

### 2. [ambiguous_requirement] No-dynamic-obstacle defaults would put the AUV permanently on a 'collision course'

**raw:** LRE-DM5 says nsRelDist/ewRelDist/obsNsVel/obsEwVel return zero when no obstacle exists; LRE-OP5 says the sensor returns 'zero-velocity and large-distance defaults'. With zero relative position a horizontal CPA gives cda = 0 and tcpa = 0, so MOM -> CAM (LRE-Beh8) would fire whenever there is no dynamic obstacle.

**fix_directive:** Resolved as: the accessors return zero exactly as LRE-DM5 states, and cda includes the vertical separation vdist(cdyn) (the safe large distance when the obstacle is absent): cda = sqrt(|p - w*tcpa|^2 + vdist^2). An absent obstacle therefore gives a large cda; a present one at the AUV's depth gives the usual horizontal CPA. Confirm that a 3-D cda with constant vertical separation is acceptable.

**java_trace:**

- `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 30-50, `compute`: LRE-OP5, LRE-DM5, LRE-Var7, LRE-Beh8, LRE-Beh14

### 3. [design_choice] CPA formulation

**raw:** LRE-OP5 does not give the CPA formula. Chosen: p = (nsRelDist, ewRelDist), taken as obstacle position minus AUV position; w = (ns_vel - obsNsVel, ew_vel - obsEwVel), the AUV velocity relative to the obstacle; tcpa = (p.w) / (|w|^2 + CPA_EPSILON). Vertical motion (rate_of_climb, obs_roc) is not used: LRE-DM5 provides no obs_roc accessor.

**fix_directive:** If ns_rel_dist/ew_rel_dist are AUV-minus-obstacle, the sign of tcpa flips: swap the operands of the velocity differences in CalcCPA.compute.

**java_trace:**

- `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 8-50, `CalcCPA`: LRE-OP5, LRE-Var7, LRE-Var8

### 4. [invented_default] CPA_EPSILON = 0.000001 keeps tcpa defined at zero relative velocity

**raw:** The operation profile forbids conditionals in compute(), so division by |w|^2 is guarded by adding CPA_EPSILON. The model's constants interface rounds a fractional constant up, so in the model CPA_EPSILON is 1: that changes the magnitude of tcpa and therefore cda, but not the sign of tcpa (the guard tcpa >= 0 is unaffected).

**fix_directive:** Accept, or provide a guard-free formulation of tcpa that the profile allows.

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 31-32, `CPA_EPSILON`: LRE-OP5, LRE-Var8

### 5. [invented_default] Safe large distance for an absent obstacle = 1000.0 m

**raw:** LRE-DM5 requires odist/hdist/vdist to return 'a safe large distance' when no obstacle exists but gives no value. 1000.0 m is far beyond every threshold (all 1.0 m).

**fix_directive:** Replace NO_OBSTACLE_DISTANCE if a specific value is required.

**java_trace:**

- `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 16-18, `NO_OBSTACLE_DISTANCE`: LRE-DM5, LRE-OP2, LRE-OP5

### 6. [invented_default] Initial sensor readings and actuator values are 0.0

**raw:** No requirement gives values before the first reading/advice. Sensor depth, velocities and rate of climb start at 0.0 (depth 0 means inOpez holds until the first update, which keeps the LRE in OCM: fail-safe). Actuator advised velocity and heading start at 0.0. Operation results start at their Java defaults (0.0, false, index 0 = no obstacle); they are recomputed before any guard is read.

**fix_directive:** Accept or supply initial values.

**java_trace:**

- `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 21-24, `Sensor`: LRE-DM5
- `java.generated.project/src/main/java/lre/actuator/Actuator.java` lines 12-13, `Actuator`: LRE-DM8

### 7. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) vs 0.1 m/s (system description)

**raw:** The system description says HCM runs at reduced speed 0.1 m/s; LRE-FR3 says on entering HCM the LRE advises 0 m/s. The requirements are authoritative, so CAUTION_SPEED = 0.0.

**fix_directive:** If 0.1 m/s is intended, set CAUTION_SPEED = 0.1 (the model will round it up to 1).

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 25-26, `CAUTION_SPEED`: LRE-FR3

### 8. [design_choice] Literal thresholds of 1 mapped to named constants

**raw:** LRE-Beh4 (vel <= 1, odist > 1), LRE-Beh9 (hvel >= 1 m/s), LRE-Beh11 (vvel >= 1 m/s) and LRE-FR2 (advise 1 m/s) use the literal 1. Speeds use NORMAL_SPEED = 1.0 (the MOM operating speed); the LRE-Beh4 distances use MIN_SAFE_DIST (default 1). Values are unchanged. LRE-Beh4's odist(cstc) > 1 is implied by 'not in OPEZ' and is kept as stated.

**fix_directive:** If the Beh4 distance bound must stay 1 when MIN_SAFE_DIST changes, introduce a separate constant.

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 22-23, `NORMAL_SPEED`: LRE-FR2, LRE-Beh4, LRE-Beh9, LRE-Beh11
- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 59-66, `step`: LRE-Beh4, LRE-Beh9, LRE-Beh11

### 9. [design_choice] Transition priority within MOM, HCM and CAM

**raw:** All transitions have priority 8. Order chosen (highest first): reqOCM, endTask (operator overrides); inOpez -> OCM; collision course -> CAM; reqHCM; then Beh9, Beh11, Beh10 -> HCM. Every branch below a guard-only branch conjoins its negation. Beh11 is placed before Beh10 because Beh10 (vdist <= dfltVert) subsumes Beh11 (vvel >= 1 and vdist <= vert) when vert <= dfltVert: in the other order Beh11's guard would be unsatisfiable (dead). All three lead to HCM with the same action.

**fix_directive:** Reorder if OPEZ should not outrank collision avoidance, or if reqHCM should outrank the safety transitions.

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 90-125, `step`: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-DC1

### 10. [design_choice] CheckOPEZ and CalcCPA re-query the closest index from the sensor

**raw:** LRE-OP2 and LRE-OP5 use the index computed by CalcCStc/CalcCDyn, but an operation's compute() may only use sensor calls and its own fields. They call sensor.closestStaticIndex()/closestDynamicIndex(), which return the same value as cstc/cdyn within a step.

**fix_directive:** None needed unless the profile allows one operation to read another's result.

**java_trace:**

- `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 25-28, `compute`: LRE-OP2
- `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 30-50, `compute`: LRE-OP5

### 11. [ambiguous_requirement] vvel is the signed rate of climb

**raw:** LRE-OP1 says 'vvel is the rate of climb', so vvel is signed and LRE-Beh11 (vvel >= 1 m/s) fires only when climbing, not when descending at 1 m/s or more.

**fix_directive:** If Beh11 should react to vertical speed in either direction, compute vvel as sqrt(rate_of_climb^2).

**java_trace:**

- `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 21-25, `compute`: LRE-OP1, LRE-Var3, LRE-Beh11

### 12. [scope_question] CAM issues no evasive advice

**raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but no requirement says what velocity or heading to advise. Entering CAM issues no output.

**fix_directive:** Specify the CAM advice (e.g. an entry advVel/advHdng) if one is required.

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 98-99, `step`: LRE-FR4, LRE-Beh8
- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 120-121, `step`: LRE-FR4, LRE-Beh14

### 13. [scope_question] MOM/HCM chattering if STATIC_OBS_DFLT_VERT_DIST > STATIC_OBS_VERT_DIST

**raw:** MOM -> HCM (Beh10: vdist <= dfltVert) and HCM -> MOM (Beh13: hdist > horiz and vdist > vert) are both guard-only. With the given defaults (all 1.0) they are mutually exclusive; if dfltVert were larger than vert, vdist in (vert, dfltVert] would enable both, giving an internal MOM/HCM cycle.

**fix_directive:** Keep dfltVert <= vert, or add vdist > dfltVert to Beh13.

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 109-112, `step`: LRE-Beh10
- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 122-124, `step`: LRE-Beh13

### 14. [design_choice] Event handling outside the listed transitions; output via actuator.apply

**raw:** step(null) means no operator input this cycle; guard-only transitions are still evaluated. Per LRE-Beh19, reqVel/reqHdng are not handled in MOM/HCM/CAM, and reqOCM/reqHCM/endTask are not handled in OCM. Outputs are sent as actuator.apply(new OutputEvent.AdvVel/AdvHdng(..)). The MOM entry advice (1 m/s) and the HCM entry advice (0 m/s) are written on every branch entering those modes; OCM has no entry advice, so only Beh7 and Beh18 advise 0 when returning to OCM.

**fix_directive:** None unless OCM entry should always advise a velocity.

**java_trace:**

- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 43-88, `step`: LRE-Beh19, LRE-Beh2, LRE-Beh3, LRE-Beh4, LRE-FR1, LRE-FR2, LRE-DM7

### 15. [design_choice] Naming to keep model names unique

**raw:** Obstacle components and the Sensor's raw readings use the requirement's snake_case names (ns_rel_dist, ns_vel, rate_of_climb, ...), and the accessor functions use its camelCase names (nsRelDist, obsNsVel, ...), so no two model elements share a name. The controller's operation fields are calcVelOp etc. to avoid a case-insensitive clash with the operation classes. Constants are UPPER_SNAKE (MIN_SAFE_DIST for minSafeDist, ...).

**fix_directive:** None.

**java_trace:**

- `java.generated.project/src/main/java/lre/sensor/Obstacle.java` lines 9-15, `Obstacle`: LRE-DM2
- `java.generated.project/src/main/java/lre/controller/LreController.java` lines 22-26, `LreController`: LRE-ARCH2

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and review the items above.
