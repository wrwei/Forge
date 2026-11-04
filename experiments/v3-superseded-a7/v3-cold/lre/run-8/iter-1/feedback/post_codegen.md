# post_codegen

**status:** uncertain

## Summary

LRE controller generated in 15 Java files (package lre): one controller with four modes and 17 transitions plus the initial mode, five operations, a sensor with an obstacle register, an actuator and sealed input/output events. It compiles with javac --release 17 -Xlint:all with no warnings, and 26 hand-written scenario checks pass. Every requirement is traced. Fourteen points the requirements did not settle are listed below.

## Issues

### 1. [invented_default] 'No obstacle' index is 0, not -1

**raw:** LRE-Var5/Var6/DM5/OP3/OP4 say the closest-obstacle index is -1 when none exists, but also make cstc/cdyn natural numbers, and the profile maps int to nat (never negative). The register reserves 0 (ObstacleRegister.NO_OBSTACLE), so identifiers start at 1. On a tie in odist, the lowest index wins.

**fix_directive:** Confirm that 0 may stand for 'none'. The controller never compares cstc/cdyn with it; the sensor turns the reserved index into a safe large distance and zero-valued fields.

**java_trace:**

- `src/main/java/lre/sensor/ObstacleRegister.java` lines 17-31, `NO_OBSTACLE`: LRE-Var5, LRE-Var6, LRE-DM3, LRE-DM5
- `src/main/java/lre/sensor/Sensor.java` lines 131-155, `closestStaticIndex`: LRE-OP3, LRE-OP4, LRE-DM5

### 2. [invented_default] Safe large distance is 100000.0 m

**raw:** LRE-DM5 says odist/hdist/vdist return 'a safe large distance' when no obstacle exists, without giving a value. NO_OBSTACLE_DIST = 100000.0, far above every threshold (all 1 m).

**fix_directive:** Confirm the value, or give one.

**java_trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 15-15, `NO_OBSTACLE_DIST`: LRE-DM5, LRE-OP2, LRE-OP5

### 3. [invented_default] CPA_EPSILON = 0.000001 keeps tcpa finite; the model rounds it up to 1

**raw:** compute() may not branch, so tcpa divides by |closing velocity|^2 + CPA_EPSILON. That keeps the divisor non-zero when the closing velocity is zero (parallel motion, or no dynamic obstacle). The model's constants interface rounds the fractional constant up to 1. In the model this changes the size of tcpa and cda but not the sign of tcpa, which is all that LRE-Beh8 and LRE-Beh14 test.

**fix_directive:** Accept the epsilon, or say how tcpa should be defined at zero closing speed.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 55-55, `CPA_EPSILON`: LRE-OP5, LRE-Var8

### 4. [design_choice] CPA formula: relative velocity, cda measured at max(tcpa, 0), vertical separation included

**raw:** LRE-OP5 gives no formula. Let p be the relative position (nsRelDist, ewRelDist) and c = AUV velocity - obstacle velocity. Then tcpa = p.c / (|c|^2 + eps), and cda = sqrt(|p - c*t|^2 + vdist^2), where t = max(tcpa, 0). Clamping t at 0 is needed: with the unclamped tcpa, a receding obstacle has cda ~ 0, so LRE-Beh18 could never fire (seen in a scenario check). Adding vdist settles a conflict: LRE-DM5 makes the field accessors return 0 when there is no obstacle, which alone would give cda = 0 and a spurious move to CAM, while LRE-OP5 speaks of 'large-distance defaults'. vdist returns the large distance in that case. obs_roc is not used: DM5 offers no accessor for it.

**fix_directive:** Confirm the CPA definition (relative or absolute obstacle velocity; 2-D or 3-D; behaviour when the closest point is already past).

**java_trace:**

- `src/main/java/lre/operation/CalcCPA.java` lines 35-49, `compute`: LRE-OP5, LRE-Var7, LRE-Var8, LRE-DM5

### 5. [design_choice] CheckOPEZ and CalcCPA get the closest index from the sensor, not from CalcCStc/CalcCDyn

**raw:** The right-hand side of a compute() may use only sensor calls, Math.sqrt, arithmetic and fields of its own class, not another operation's getter. So CheckOPEZ calls sensor.closestStaticIndex(), and CalcCPA calls sensor.closestDynamicIndex(): the same functions CalcCStc and CalcCDyn use, giving the same value within a step. step() still calls CalcCStc before CheckOPEZ and CalcCDyn before CalcCPA.

**fix_directive:** Accept, or allow an operation to read another operation's result.

**java_trace:**

- `src/main/java/lre/operation/CheckOPEZ.java` lines 23-26, `compute`: LRE-OP2, LRE-OP3
- `src/main/java/lre/operation/CalcCPA.java` lines 36-36, `cpaIndex`: LRE-OP5, LRE-OP4

### 6. [design_choice] State variables live in the operation classes

**raw:** LRE-Var1..Var8 say 'the controller maintains' each variable. The variables are fields of the operation that computes them (as the trace rules allow), and the predicates read them through the getters. They are not copied into controller fields, which would duplicate the names and add assignments outside the profile's step() shape.

**fix_directive:** None unless controller-level fields are required.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 53-76, `step`: LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8

### 7. [design_choice] Transition order within each mode

**raw:** All LRE-Beh requirements have the same priority. Order chosen: the operator's reqOCM first, then inOpez -> OCM, then collision -> CAM, then endTask and reqHCM, then the autonomous HCM triggers in Beh9, Beh10, Beh11 order. As the profile requires, each branch below a guard-only branch conjoins that guard's negation. 17 transitions plus the initial mode, one per Beh requirement (LRE-DC1).

**fix_directive:** Confirm the order, especially collision avoidance before endTask and reqHCM in MOM.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 79-136, `step`: LRE-Beh2, LRE-Beh3, LRE-Beh4, LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1

### 8. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) or 0.1 m/s (system description)

**raw:** The system description gives HCM a reduced speed of 0.1 m/s; LRE-FR3 says that on entering HCM the LRE advises 0 m/s. The implementation follows LRE-FR3 (HCM_ADVISED_VEL = 0.0).

**fix_directive:** Confirm 0 m/s.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 47-47, `HCM_ADVISED_VEL`: LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 9. [ambiguous_requirement] vvel is the signed rate of climb, so only climbing triggers LRE-Beh11

**raw:** LRE-OP1 defines vvel as the rate of climb, and LRE-Beh11 tests vvel >= 1, so descending fast towards a static obstacle does not trigger it (LRE-Beh10 still applies within the default vertical distance). Implemented as written, without an absolute value.

**fix_directive:** Say whether LRE-Beh11 should test |vvel|.

**java_trace:**

- `src/main/java/lre/operation/CalcVel.java` lines 25-25, `compute`: LRE-OP1, LRE-Var3, LRE-Beh11
- `src/main/java/lre/controller/LreController.java` lines 68-68, `step`: LRE-Beh11

### 10. [ambiguous_requirement] HCM -> MOM (LRE-Beh13) ignores why HCM was entered; it relies on default vertical distance <= vertical distance

**raw:** When no static obstacle is near, LRE-Beh13 takes HCM back to MOM on the next step, even if the operator's reqHCM caused the entry. The MOM->HCM and HCM->MOM guards cannot both hold only when STATIC_OBS_DFLT_VERT_DIST <= STATIC_OBS_VERT_DIST. That holds for the defaults (both 1). Otherwise the two modes would alternate with no event, advising 1 and 0 m/s in turn.

**fix_directive:** Confirm both behaviours, or add the relation between the thresholds as a requirement.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 124-127, `step`: LRE-Beh13, LRE-Beh10, LRE-Beh12, LRE-DM4

### 11. [scope_question] CAM's evasive manoeuvre has no specified output

**raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but no requirement gives the advVel/advHdng values. CAM issues no output; it only leaves via LRE-Beh17 or LRE-Beh18.

**fix_directive:** Specify CAM's entry or step advice, if any.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 129-136, `step`: LRE-FR4

### 12. [design_choice] A cycle with no operator input is step(null)

**raw:** step(InputEvent) runs once per cycle and also takes the autonomous transitions. When no input arrived, the caller passes null, and no event-triggered branch matches. No extra 'tick' event was invented: LRE-DM6 lists exactly six inputs.

**fix_directive:** None unless an explicit no-input event is wanted.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 46-52, `step`: LRE-ARCH2

### 13. [design_choice] The literal limits of 1 in Beh4, Beh9 and Beh11 are separate constants

**raw:** LRE-Beh4 says vel <= 1 and odist > 1. LRE-Beh9 and LRE-Beh11 say hvel and vvel >= 1 m/s. These are not stated as minSafeDist or the normal speed, so each has its own constant (MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_DIST, HORIZ_CAUTION_VEL, VERT_CAUTION_VEL), all 1.0.

**fix_directive:** Say if any of them should be tied to an LRE-DM4 threshold.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 25-39, `LreConstants`: LRE-Beh4, LRE-Beh9, LRE-Beh11

### 14. [invented_default] Actuator stores 0.0 before any advice

**raw:** LRE-DM8 says the actuator keeps the latest advised values but gives none before the first advice. Both start at 0.0.

**fix_directive:** Confirm.

**java_trace:**

- `src/main/java/lre/actuator/Actuator.java` lines 12-13, `Actuator`: LRE-DM8

## Next step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and review the points above.
