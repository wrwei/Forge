# Post-codegen review — LRE iteration 1

**status:** uncertain

LRE iteration 1: 15 Java files written from scratch under src/main/java/lre/; all 51 requirements traced. Eight points need human confirmation, chiefly the conflicting no-obstacle defaults between LRE-DM5 and LRE-OP5 and the HCM advised-velocity discrepancy between the system description and LRE-FR3.

## Issues

### 1. Safe large distance value not specified (`invented_default`)

- **Requirements:** LRE-DM5
- **Java:** `src/main/java/lre/constants/LreConstants.java` → `SAFE_LARGE_DIST`
- **Raw:** LRE-DM5 says odist/hdist/vdist return 'a safe large distance' when no obstacle exists but gives no value.
- **Fix directive:** Confirm LreConstants.SAFE_LARGE_DIST = 1000.0 m is acceptable, or supply the intended value.

### 2. Conflicting no-obstacle defaults for nsRelDist/ewRelDist (`ambiguous_requirement`)

- **Requirements:** LRE-DM5, LRE-OP5
- **Java:** `src/main/java/lre/sensor/Sensor.java` → `nsRelDist`
- **Raw:** LRE-DM5 says field accessors nsRelDist/ewRelDist/obsNsVel/obsEwVel return zero when no obstacle exists; LRE-OP5 says the no-dynamic-obstacle case is handled by 'zero-velocity and LARGE-DISTANCE defaults'. Zero relative distances make CalcCPA yield cda=0, which spuriously enables MOM->CAM (LRE-Beh8) whenever there is no dynamic obstacle.
- **Fix directive:** Resolved in favour of LRE-OP5: Sensor.nsRelDist/ewRelDist return SAFE_LARGE_DIST when the index is absent, while obsNsVel/obsEwVel return zero. Confirm, or amend LRE-DM5.

### 3. CPA formula and division-by-zero guard (`invented_default`)

- **Requirements:** LRE-OP5, LRE-Var7, LRE-Var8
- **Java:** `src/main/java/lre/operation/CalcCPA.java` → `compute`
- **Raw:** LRE-OP5 names cda and tcpa but gives no formula. Standard closest-point-of-approach used: tcpa = -(r.v)/|v|^2, cda = |r + v*tcpa|, with r the relative position and v the obstacle-minus-AUV relative velocity. 1.0E-6 is added to |v|^2 so the division is total.
- **Fix directive:** Confirm the CPA formulation and the epsilon, or specify the intended definitions of cda/tcpa.

### 4. HCM advised velocity: 0.1 m/s or 0 m/s (`ambiguous_requirement`)

- **Requirements:** LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh9, LRE-FR3
- **Java:** `src/main/java/lre/controller/LreController.java` → `step`
- **Raw:** system_description.txt describes HCM as 'Reduced speed (0.1 m/s)'; LRE-FR3 says 'On entering HCM, the LRE advises a velocity of 0 m/s'.
- **Fix directive:** Implemented LRE-FR3 (advVel(0)) because requirement_all.json is the canonical source. Reconcile the system description if 0.1 m/s was intended.

### 5. State variables held on the controller and refreshed at the top of step() (`design_choice`)

- **Requirements:** LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8
- **Java:** `src/main/java/lre/controller/LreController.java` → `step`
- **Raw:** LRE-Var1..8 say 'the controller maintains' the variable, while the values are produced by the operation classes.
- **Fix directive:** Each operation keeps its own field and the controller copies it into a like-named field at the start of step(), before the guard predicates. Both locations are recorded in result_codegen.json.

### 6. Operator payload captured into state variables before pass-through (`design_choice`)

- **Requirements:** LRE-Beh2, LRE-Beh3
- **Java:** `src/main/java/lre/controller/LreController.java` → `reqVelValue`
- **Raw:** LRE-Beh2/LRE-Beh3 pass reqVel/reqHdng straight through to advVel/advHdng.
- **Fix directive:** The payload is first assigned to reqVelValue / reqHdngValue and then emitted, which is the typed-trigger rebinding shape CLAUDE.md documents for non-trivial event payloads.

### 7. cstc/cdyn annotated nat although -1 is a legal value (`design_choice`)

- **Requirements:** LRE-Var5, LRE-Var6
- **Java:** `src/main/java/lre/controller/LreController.java` → `cstc`
- **Raw:** LRE-Var5/LRE-Var6 call cstc and cdyn natural numbers yet define -1 as the no-obstacle sentinel.
- **Fix directive:** Annotated @RoboChartType("nat") per the requirement wording. No guard tests the sentinel; the Sensor layer converts an absent index into a safe default, per the codegen rules.

### 8. LRE-Beh1 has no transition element (`scope_question`)

- **Requirements:** LRE-Beh1, LRE-FR1
- **Java:** `src/main/java/lre/controller/LreController.java` → `currentMode`
- **Raw:** LRE-Beh1 ('On power-up the LRE begins in OCM') is an initialisation fact rather than a transition.
- **Fix directive:** Mapped to the currentMode field initialiser (= LreMode.OCM). No branch in step() corresponds to it.

## Next step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers).
