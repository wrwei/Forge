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
*** Type unification failed: No type arity lens_ext :: plus
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

### Issue 3: isabelle_other — Isabelle error: *** Operator:  (+) :: ??'a ⇒ ??'a ⇒ ??'a

**Raw**
```
*** *** Operator:  (+) :: ??'a ⇒ ??'a ⇒ ??'a
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: Operand:   clock :: ??'b ⟹ ??'c MovementController_scheme

**Raw**
```
*** Operand:   clock :: ??'b ⟹ ??'c MovementController_scheme
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
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: No type arity lens_ext :: plus

**Raw**
```
*** No type arity lens_ext :: plus
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: *** Now trying to infer coercions globally.

**Raw**
```
*** *** Now trying to infer coercions globally.
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: *** Coercion inference failed:

**Raw**
```
*** *** Coercion inference failed:
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: weak unification of subtype constraints fails

**Raw**
```
*** weak unification of subtype constraints fails
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:190
```

**Java trace**
  - Java: `MovementController_Beh.thy`:190

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: Occurs check!

**Raw**
```
*** Occurs check!
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:190
```

**Java trace**
  - Java: `MovementController_Beh.thy`:190

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: *** At command "zoperation" (line 190 of "/Users/ranwei/Git/Forge/forge.transfor

**Raw**
```
*** *** At command "zoperation" (line 190 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:190
```

**Java trace**
  - Java: `MovementController_Beh.thy`:190

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
(none identified)

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
