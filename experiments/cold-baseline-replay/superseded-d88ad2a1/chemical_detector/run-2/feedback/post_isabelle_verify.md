# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 14
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: Extra variables on rhs: "CurrentDistance"

**Raw**
```
*** Extra variables on rhs: "CurrentDistance"
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: The error(s) above occurred in definition "WaitingToWaiting_1_type_def":

**Raw**
```
*** The error(s) above occurred in definition "WaitingToWaiting_1_type_def":
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: "WaitingToWaiting_1_type ≡

**Raw**
```
*** "WaitingToWaiting_1_type ≡
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: (SIGMA _:CurrentDistance.

**Raw**
```
*** (SIGMA _:CurrentDistance.
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:167
```

**Java trace**
  - Java: `MovementController_Beh.thy`:167

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: {{}, {resume}, {Evt.stop}, {turn}, {odometer}})⇩e"

**Raw**
```
*** {{}, {resume}, {Evt.stop}, {turn}, {odometer}})⇩e"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:167
```

**Java trace**
  - Java: `MovementController_Beh.thy`:167

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: At command "zoperation" (line 167 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 167 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:167
```

**Java trace**
  - Java: `MovementController_Beh.thy`:167

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_type_mismatch — Type unification failed

**Raw**
```
*** Type unification failed: Clash of types "ℤ" and "_ Intensity_scheme"
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
An expression in the generated theory has incompatible types. Often this is because the auto-emitted `consts` signature (`unit \<Rightarrow> integer`) is too narrow for an actual call site. Refine the signature manually in the session theory, or extend phase 5c to infer it from the Java method's parameters.

### Issue 8: isabelle_other — Isabelle error: *** Type error in application: incompatible operand type

**Raw**
```
*** *** Type error in application: incompatible operand type
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: *** Operator:  goreq :: Intensity × Intensity ⇒ 𝔹

**Raw**
```
*** *** Operator:  goreq :: Intensity × Intensity ⇒ 𝔹
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: Operand:   (ins<𝗌'>, thr ()) :: Intensity × ℤ

**Raw**
```
*** Operand:   (ins<𝗌'>, thr ()) :: Intensity × ℤ
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: *** Coercion Inference:

**Raw**
```
*** *** Coercion Inference:
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: *** Local coercion insertion on the operand failed:

**Raw**
```
*** *** Local coercion insertion on the operand failed:
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:171
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:171

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 13: isabelle_other — Isabelle error: No coercion known for type constructors: "int" and "Intensity_ext"

**Raw**
```
*** No coercion known for type constructors: "int" and "Intensity_ext"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:171
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:171

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 14: isabelle_other — Isabelle error: At command "zoperation" (line 171 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 171 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:171
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:171

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- Angle.java
- GAMode.java
- Loc.java
- MVMode.java
- MovementController.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
