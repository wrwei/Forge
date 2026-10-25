# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: Extra variables on rhs: "D0"

**Raw**
```
*** Extra variables on rhs: "D0"
In theory: Movement_Beh
```

**Java trace**
  - Java: `Movement_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: The error(s) above occurred in definition "AvoidingToAvoiding_type_def":

**Raw**
```
*** The error(s) above occurred in definition "AvoidingToAvoiding_type_def":
In theory: Movement_Beh
```

**Java trace**
  - Java: `Movement_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: "AvoidingToAvoiding_type ≡

**Raw**
```
*** "AvoidingToAvoiding_type ≡
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/Movement_Beh.thy:233
```

**Java trace**
  - Java: `Movement_Beh.thy`:233

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: (SIGMA _:D0. {{}, {resume}, {turn}, {Evt.stop}, {odometer}})⇩e"

**Raw**
```
*** (SIGMA _:D0. {{}, {resume}, {turn}, {Evt.stop}, {odometer}})⇩e"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/Movement_Beh.thy:233
```

**Java trace**
  - Java: `Movement_Beh.thy`:233

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: At command "zoperation" (line 233 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 233 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/Movement_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/Movement_Beh.thy:233
```

**Java trace**
  - Java: `Movement_Beh.thy`:233

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- Angle.java
- GasAnalysis.java
- GasAnalysisMode.java
- Loc.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
