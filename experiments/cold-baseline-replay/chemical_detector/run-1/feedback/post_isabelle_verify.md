# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: Extra variables on rhs: "OdometerValue"

**Raw**
```
*** Extra variables on rhs: "OdometerValue"
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
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:164
```

**Java trace**
  - Java: `MovementController_Beh.thy`:164

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: (SIGMA _:OdometerValue. {{}, {turn}, {Evt.stop}, {resume}, {odometer}})⇩e"

**Raw**
```
*** (SIGMA _:OdometerValue. {{}, {turn}, {Evt.stop}, {resume}, {odometer}})⇩e"
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:164
```

**Java trace**
  - Java: `MovementController_Beh.thy`:164

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: At command "zoperation" (line 164 of "/Users/ranwei/Git/Forge/forge.transformati

**Raw**
```
*** At command "zoperation" (line 164 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:164
```

**Java trace**
  - Java: `MovementController_Beh.thy`:164

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- Angle.java
- ChangeDirection.java
- GasAnalysisMode.java
- GasAnalysisOutput.java
- Loc.java
- MovementController.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
