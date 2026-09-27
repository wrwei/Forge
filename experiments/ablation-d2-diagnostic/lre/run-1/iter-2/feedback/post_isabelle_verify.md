# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 8
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- Verification summary

## Issues
### Issue 1: isabelle_type_mismatch — Type unification failed

**Raw**
```
*** Type unification failed: Clash of types "_ ⇒ _" and "ℝ"
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
An expression in the generated theory has incompatible types. Often this is because the auto-emitted `consts` signature (`unit \<Rightarrow> integer`) is too narrow for an actual call site. Refine the signature manually in the session theory, or extend phase 5c to infer it from the Java method's parameters.

### Issue 2: isabelle_other — Isabelle error: *** Type error in application: operator not of function type

**Raw**
```
*** *** Type error in application: operator not of function type
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: *** Operator:  LreController.cda<𝗌> :: ℝ

**Raw**
```
*** *** Operator:  LreController.cda<𝗌> :: ℝ
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: Operand:   closestDynamicIndex () :: ℤ

**Raw**
```
*** Operand:   closestDynamicIndex () :: ℤ
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: *** Coercion Inference:

**Raw**
```
*** *** Coercion Inference:
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: *** Local coercion insertion on the operator failed:

**Raw**
```
*** *** Local coercion insertion on the operator failed:
At: /Users/ranwei/Gitee/forge-d2-run3-lre/forge.transformations/output/isabelle/LreController_Beh.thy:100
```

**Java trace**
  - Java: `LreController_Beh.thy`:100

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: No complex coercion from "real" to "fun"

**Raw**
```
*** No complex coercion from "real" to "fun"
At: /Users/ranwei/Gitee/forge-d2-run3-lre/forge.transformations/output/isabelle/LreController_Beh.thy:100
```

**Java trace**
  - Java: `LreController_Beh.thy`:100

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: At command "zoperation" (line 100 of "/Users/ranwei/Gitee/forge-d2-run3-lre/forg

**Raw**
```
*** At command "zoperation" (line 100 of "/Users/ranwei/Gitee/forge-d2-run3-lre/forge.transformations/output/isabelle/LreController_Beh.thy")
At: /Users/ranwei/Gitee/forge-d2-run3-lre/forge.transformations/output/isabelle/LreController_Beh.thy:100
```

**Java trace**
  - Java: `LreController_Beh.thy`:100

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
(none identified)

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
