# post_codegen — LRE iteration 1

status: uncertain

Cold code generation of the LRE safety controller from requirement_all.json (51 requirements).
15 Java files under `java.generated.project/src/main/java/lre/`; `./gradlew build` is clean.
All 51 requirements are traced in `result_codegen.json`.

## Issues

### 1. Sensor safe-large-distance default is unspecified (`invented_default`)

**Requirements:** LRE-DM5

LRE-DM5 requires odist/hdist/vdist to return 'a safe large distance' when no obstacle exists but gives no value.

**Fix directive:** Confirm 1000.0 m is an acceptable no-obstacle distance, or supply the intended value for Sensor.SAFE_LARGE_DIST.

### 2. CPA division guarded by an invented epsilon (`invented_default`)

**Requirements:** LRE-OP5, LRE-Var7, LRE-Var8

LRE-OP5 specifies cda/tcpa but not the zero-relative-velocity case, where tcpa = -(r.v)/|v|^2 divides by zero.

**Fix directive:** Confirm flooring the squared relative speed at 1e-6 (Sensor.relSpeedSq) is acceptable, or state the intended tcpa/cda when the obstacle closes at zero relative speed.

### 3. CAM entry action is unspecified (`ambiguous_requirement`)

**Requirements:** LRE-FR4

LRE-FR4 says the AUV 'performs evasive manoeuvres' in CAM, but LRE-DM7 allows only advVel/advHdng and no requirement states either value for CAM.

**Fix directive:** Specify the advised velocity and/or heading the LRE must issue on entering CAM. No entry action was invented.

### 4. Operator-requested transitions given priority over autonomous ones (`design_choice`)

**Requirements:** LRE-Beh12, LRE-Beh15, LRE-Beh17, LRE-Beh6

The requirements state no relative priority between the unconditional operator transitions (Beh6, Beh7, Beh12, Beh15, Beh17) and the guarded autonomous ones (Beh5, Beh8-Beh11, Beh13, Beh14, Beh16, Beh18).

**Fix directive:** Confirm that reqOCM/endTask/reqHCM take precedence within a mode; the alternative (safety guards first) would shadow Beh6/Beh15/Beh17, which are stated unconditionally.

### 5. cstc/cdyn read from the Sensor selection functions inside each operation (`design_choice`)

**Requirements:** LRE-OP2, LRE-OP5

LRE-OP2 says CheckOPEZ 'uses the index computed by CalcCStc' and LRE-OP5 says CalcCPA uses cdyn. compute() bodies may not contain local variables or calls on other operation objects, so both re-read sensor.closestStaticIndex()/closestDynamicIndex() -- the same functions CalcCStc/CalcCDyn wrap, so the values coincide within a step.

**Fix directive:** Confirm this is acceptable, or specify an operation-to-operation wiring that keeps compute() bodies to direct this.field = expression assignments.

### 6. Literal 1.0 thresholds in LRE-Beh4 and LRE-Beh9/Beh11 not lifted to constants (`design_choice`)

**Requirements:** LRE-Beh11, LRE-Beh4, LRE-Beh9

LRE-DM4 declares exactly four constants. Beh4 (vel <= 1, odist > 1) and Beh9/Beh11 (hvel/vvel >= 1 m/s) use numeric 1 that is not one of those four.

**Fix directive:** Confirm these are fixed literals, or add named constants for them to LRE-DM4.

### 7. cstc/cdyn are Java int but extracted as RoboChart nat (`scope_question`)

**Requirements:** LRE-Var5, LRE-Var6

LRE-Var5/Var6 call cstc/cdyn natural numbers whose -1 value means 'no obstacle'. Java int maps to RoboChart nat, so -1 is not representable in the extracted model.

**Fix directive:** No controller guard tests the sentinel: the Sensor returns safe defaults for index -1, per LRE-DM5. Confirm the sentinel is intended to stay inside the Sensor layer.

## next_step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers).
None of the issues above block extraction; they are judgement calls for human review.
