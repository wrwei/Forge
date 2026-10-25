# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_type_mismatch — Type unification failed

**Raw**
```
*** Type unification failed: Clash of types "ℤ" and "_ list"
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
An expression in the generated theory has incompatible types. Often this is because the auto-emitted `consts` signature (`unit \<Rightarrow> integer`) is too narrow for an actual call site. Refine the signature manually in the session theory, or extend phase 5c to infer it from the Java method's parameters.

### Issue 2: isabelle_other — Isabelle error: *** Type error in application: incompatible operand type

**Raw**
```
*** *** Type error in application: incompatible operand type
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: *** Operator:  classify :: GasSensor list ⇒ Status

**Raw**
```
*** *** Operator:  classify :: GasSensor list ⇒ Status
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: Operand:   MovementController_Beh.input () :: ℤ

**Raw**
```
*** Operand:   MovementController_Beh.input () :: ℤ
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: *** Coercion Inference:

**Raw**
```
*** *** Coercion Inference:
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: *** Local coercion insertion on the operand failed:

**Raw**
```
*** *** Local coercion insertion on the operand failed:
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:128
```

**Java trace**
  - Java: `MovementController_Beh.thy`:128

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: No coercion known for type constructors: "int" and "list"

**Raw**
```
*** No coercion known for type constructors: "int" and "list"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:128
```

**Java trace**
  - Java: `MovementController_Beh.thy`:128

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: At command "zoperation" (line 128 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 128 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:128
```

**Java trace**
  - Java: `MovementController_Beh.thy`:128

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_parse_error — Theory will not parse

**Raw**
```
*** Outer syntax error (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy"): name expected,
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:31
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:31

**Fix directive**
Isabelle's outer syntax parser rejected the generated .thy file. The line number above points at the offending construct. This is usually a template-side bug — see docs/fixes/I2_template_fork.md for known classes.

### Issue 10: isabelle_other — Isabelle error: but keyword update (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/ou

**Raw**
```
*** but keyword update (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy") was found
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:31
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:31

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_parse_error — Theory will not parse

**Raw**
```
*** At command "<malformed>" (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:31
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:31

**Fix directive**
Isabelle's outer syntax parser rejected the generated .thy file. The line number above points at the offending construct. This is usually a template-side bug — see docs/fixes/I2_template_fork.md for known classes.

## Files to review
- Analysis.java
- Angle.java
- ChangeDirection.java
- GasAnalysisController.java
- GasAnalysisMode.java
- GasSensorService.java
- IntensityCompute.java
- Loc.java
- LocationCompute.java
- MovementMode.java
- MovementSensorService.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
