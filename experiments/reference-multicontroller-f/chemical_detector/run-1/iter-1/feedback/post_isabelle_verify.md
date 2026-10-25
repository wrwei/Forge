# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 220 theory marker(s) seen before failure.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: /tmp/isabelle-/bash_script7712529048815874256: line 2: lualatex: command not fou

**Raw**
```
*** /tmp/isabelle-/bash_script7712529048815874256: line 2: lualatex: command not found
In theory: ITrees
```

**Java trace**
  - Java: `ITrees`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: Failed to build document "document"

**Raw**
```
*** Failed to build document "document"
In theory: ITrees
```

**Java trace**
  - Java: `ITrees`

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
