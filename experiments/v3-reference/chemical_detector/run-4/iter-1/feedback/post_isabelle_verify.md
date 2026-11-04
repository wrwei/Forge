# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 26
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: Coercion inference failed:

**Raw**
```
*** Coercion inference failed:
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: no supremum

**Raw**
```
*** no supremum
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: *** Cannot fulfil subtype constraints:

**Raw**
```
*** *** Cannot fulfil subtype constraints:
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: Loc  <:  ??'a   from function application  side () = right

**Raw**
```
*** Loc  <:  ??'a   from function application  side () = right
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: Loc  <:  ??'a   from function application  side () = front

**Raw**
```
*** Loc  <:  ??'a   from function application  side () = front
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: Loc  <:  ??'a   from function application  side () = left

**Raw**
```
*** Loc  <:  ??'a   from function application  side () = left
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: MovementController ⇒ ℝ  <:  MovementController ⇒ ℝ

**Raw**
```
*** MovementController ⇒ ℝ  <:  MovementController ⇒ ℝ
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: from function application

**Raw**
```
*** from function application
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: [st ↝ Waiting, evadeVelocity ↝ lv (), turnRight ↝ side () = left,

**Raw**
```
*** [st ↝ Waiting, evadeVelocity ↝ lv (), turnRight ↝ side () = left,
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: turnLeft ↝ side () = right, turnBack ↝ side () = front,

**Raw**
```
*** turnLeft ↝ side () = right, turnBack ↝ side () = front,
At: /Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/MovementController_Beh.thy:131
```

**Java trace**
  - Java: `MovementController_Beh.thy`:131

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: velocity ↝ speed ()]

**Raw**
```
*** velocity ↝ speed ()]
At: /Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/MovementController_Beh.thy:131
```

**Java trace**
  - Java: `MovementController_Beh.thy`:131

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: At command "zoperation" (line 131 of "/Users/ranwei/Gitee/forge-v3r7-run4-chemic

**Raw**
```
*** At command "zoperation" (line 131 of "/Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/MovementController_Beh.thy:131
```

**Java trace**
  - Java: `MovementController_Beh.thy`:131

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 13: isabelle_type_mismatch — Type unification failed

**Raw**
```
*** Type unification failed: Clash of types "Loc" and "ℝ"
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
An expression in the generated theory has incompatible types. Often this is because the auto-emitted `consts` signature (`unit \<Rightarrow> integer`) is too narrow for an actual call site. Refine the signature manually in the session theory, or extend phase 5c to infer it from the Java method's parameters.

### Issue 14: isabelle_other — Isabelle error: *** Type error in application: incompatible operand type

**Raw**
```
*** *** Type error in application: incompatible operand type
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 15: isabelle_other — Isabelle error: *** Operator:  (=) (side ()) :: ℝ ⇒ 𝔹

**Raw**
```
*** *** Operator:  (=) (side ()) :: ℝ ⇒ 𝔹
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 16: isabelle_other — Isabelle error: Operand:   left :: Loc

**Raw**
```
*** Operand:   left :: Loc
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 17: isabelle_other — Isabelle error: *** Coercion Inference:

**Raw**
```
*** *** Coercion Inference:
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 18: isabelle_other — Isabelle error: *** Local coercion insertion on the operand failed:

**Raw**
```
*** *** Local coercion insertion on the operand failed:
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 19: isabelle_other — Isabelle error: "Loc" is not a subtype of "ℝ"

**Raw**
```
*** "Loc" is not a subtype of "ℝ"
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 20: isabelle_other — Isabelle error: *** Now trying to infer coercions globally.

**Raw**
```
*** *** Now trying to infer coercions globally.
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 21: isabelle_other — Isabelle error: *** Coercion inference failed:

**Raw**
```
*** *** Coercion inference failed:
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 22: isabelle_other — Isabelle error: GasAnalysisController ⇒ ℝ  <:  GasAnalysisController ⇒ ℝ

**Raw**
```
*** GasAnalysisController ⇒ ℝ  <:  GasAnalysisController ⇒ ℝ
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 23: isabelle_other — Isabelle error: [st ↝ Reading, evadeVelocity ↝ lv (), turnRight ↝ side () = left,

**Raw**
```
*** [st ↝ Reading, evadeVelocity ↝ lv (), turnRight ↝ side () = left,
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 24: isabelle_other — Isabelle error: turnLeft ↝ side () = right, turnBack ↝ side () = front,

**Raw**
```
*** turnLeft ↝ side () = right, turnBack ↝ side () = front,
At: /Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:116
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:116

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 25: isabelle_other — Isabelle error: velocity ↝ speed ()]

**Raw**
```
*** velocity ↝ speed ()]
At: /Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:116
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:116

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 26: isabelle_other — Isabelle error: At command "zoperation" (line 116 of "/Users/ranwei/Gitee/forge-v3r7-run4-chemic

**Raw**
```
*** At command "zoperation" (line 116 of "/Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r7-run4-chemical_detector/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:116
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:116

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- Angle.java
- ChangeDirection.java
- GasAnalysisMode.java
- Loc.java
- Move.java
- MovementController.java
- MovementMode.java
- RandomWalk.java
- ShortRandomWalk.java
- Status.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
