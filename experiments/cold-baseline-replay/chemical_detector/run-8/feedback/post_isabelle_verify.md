# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 12
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

### Issue 3: isabelle_other — Isabelle error: *** Operator:  copyReading :: GasSensor list ⇒ GasSensor

**Raw**
```
*** *** Operator:  copyReading :: GasSensor list ⇒ GasSensor
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: Operand:   v () :: ℤ

**Raw**
```
*** Operand:   v () :: ℤ
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
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:119
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:119

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: No coercion known for type constructors: "int" and "list"

**Raw**
```
*** No coercion known for type constructors: "int" and "list"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:119
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:119

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: At command "zoperation" (line 119 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 119 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:119
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:119

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: Failed to apply initial proof method (line 176 of "/Users/ranwei/Git/Forge/forge

**Raw**
```
*** Failed to apply initial proof method (line 176 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy"):
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:176
```

**Java trace**
  - Java: `MovementController_Beh.thy`:176

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: goal:

**Raw**
```
*** goal:
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:176
```

**Java trace**
  - Java: `MovementController_Beh.thy`:176

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: No subgoals!

**Raw**
```
*** No subgoals!
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:176
```

**Java trace**
  - Java: `MovementController_Beh.thy`:176

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: At command "by" (line 176 of "/Users/ranwei/Git/Forge/forge.transformations/outp

**Raw**
```
*** At command "by" (line 176 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:176
```

**Java trace**
  - Java: `MovementController_Beh.thy`:176

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
(none identified)

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
