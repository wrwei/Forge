# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Tactic timeout, suspected in GasAnalysisController_deadlock_free (deadlock_free — inferred from .thy)
- Tactic interrupt — cascade from timeout in GasAnalysisController_deadlock_free

## Issues
### Issue 1: isabelle_other — Isabelle error: Duplicate constant declaration "GasAnalysisController_Beh.SeqGs" vs. "GasAnalysi

**Raw**
```
*** Duplicate constant declaration "GasAnalysisController_Beh.SeqGs" vs. "GasAnalysisController_Beh.SeqGs" (line 60 of "/mnt/c/Users/Will/Gitee/fmgvc-cd-run6-redo/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy")
At: /mnt/c/Users/Will/Gitee/fmgvc-cd-run6-redo/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:60
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:60

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: At command "definition" (line 60 of "/mnt/c/Users/Will/Gitee/fmgvc-cd-run6-redo/

**Raw**
```
*** At command "definition" (line 60 of "/mnt/c/Users/Will/Gitee/fmgvc-cd-run6-redo/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy")
At: /mnt/c/Users/Will/Gitee/fmgvc-cd-run6-redo/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:60
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:60

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- Angle.java
- GasAnalysisMode.java
- Loc.java
- MovementController.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
