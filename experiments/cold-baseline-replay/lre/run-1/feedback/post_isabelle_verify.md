# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 20
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_type_mismatch — Type unification failed

**Raw**
```
*** Type unification failed: Clash of types "_ × _" and "ℝ"
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
An expression in the generated theory has incompatible types. Often this is because the auto-emitted `consts` signature (`unit \<Rightarrow> integer`) is too narrow for an actual call site. Refine the signature manually in the session theory, or extend phase 5c to infer it from the Java method's parameters.

### Issue 2: isabelle_other — Isabelle error: *** Type error in application: incompatible operand type

**Raw**
```
*** *** Type error in application: incompatible operand type
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: *** Operator:  LreController_Beh.max :: ℝ ⇒ ℝ

**Raw**
```
*** *** Operator:  LreController_Beh.max :: ℝ ⇒ ℝ
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: Operand:

**Raw**
```
*** Operand:
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: ((0::??'a) / (10::??'a),

**Raw**
```
*** ((0::??'a) / (10::??'a),
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: nsRelDist (closestDynamicIndex ()) * nsRelDist (closestDynamicIndex ()) +

**Raw**
```
*** nsRelDist (closestDynamicIndex ()) * nsRelDist (closestDynamicIndex ()) +
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: ewRelDist (closestDynamicIndex ()) * ewRelDist (closestDynamicIndex ()) -

**Raw**
```
*** ewRelDist (closestDynamicIndex ()) * ewRelDist (closestDynamicIndex ()) -
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: (nsRelDist (closestDynamicIndex ()) *

**Raw**
```
*** (nsRelDist (closestDynamicIndex ()) *
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: - obsNsVel (closestDynamicIndex ()) +

**Raw**
```
*** - obsNsVel (closestDynamicIndex ()) +
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: ewRelDist (closestDynamicIndex ()) *

**Raw**
```
*** ewRelDist (closestDynamicIndex ()) *
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: - obsEwVel (closestDynamicIndex ())) *

**Raw**
```
*** - obsEwVel (closestDynamicIndex ())) *
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: - obsEwVel (closestDynamicIndex ())) /

**Raw**
```
*** - obsEwVel (closestDynamicIndex ())) /
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 13: isabelle_other — Isabelle error: (obsNsVel (closestDynamicIndex ()) * obsNsVel (closestDynamicIndex ()) +

**Raw**
```
*** (obsNsVel (closestDynamicIndex ()) * obsNsVel (closestDynamicIndex ()) +
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 14: isabelle_other — Isabelle error: obsEwVel (closestDynamicIndex ()) * obsEwVel (closestDynamicIndex ()) +

**Raw**
```
*** obsEwVel (closestDynamicIndex ()) * obsEwVel (closestDynamicIndex ()) +
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 15: isabelle_other — Isabelle error: 1 / 1000000000)) ::

**Raw**
```
*** 1 / 1000000000)) ::
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 16: isabelle_other — Isabelle error: ??'a × ℝ

**Raw**
```
*** ??'a × ℝ
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 17: isabelle_other — Isabelle error: *** Coercion Inference:

**Raw**
```
*** *** Coercion Inference:
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 18: isabelle_other — Isabelle error: *** Local coercion insertion on the operand failed:

**Raw**
```
*** *** Local coercion insertion on the operand failed:
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/LreController_Beh.thy:111
```

**Java trace**
  - Java: `LreController_Beh.thy`:111

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 19: isabelle_other — Isabelle error: No coercion known for type constructors: "prod" and "real"

**Raw**
```
*** No coercion known for type constructors: "prod" and "real"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/LreController_Beh.thy:111
```

**Java trace**
  - Java: `LreController_Beh.thy`:111

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 20: isabelle_other — Isabelle error: At command "zoperation" (line 111 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 111 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/LreController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/LreController_Beh.thy:111
```

**Java trace**
  - Java: `LreController_Beh.thy`:111

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- CalcCDyn.java
- CalcCPA.java
- CalcCStc.java
- CalcVel.java
- CheckOPEZ.java
- LreController.java
- LreMode.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
