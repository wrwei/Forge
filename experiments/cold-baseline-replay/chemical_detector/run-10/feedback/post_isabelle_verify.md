# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 2 theory marker(s) seen before failure.

## Run history
- New this run: 7
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: Failed to apply initial proof method (line 173 of "/Users/ranwei/Git/Forge/forge

**Raw**
```
*** Failed to apply initial proof method (line 173 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy"):
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:173
```

**Java trace**
  - Java: `MovementController_Beh.thy`:173

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: goal:

**Raw**
```
*** goal:
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:173
```

**Java trace**
  - Java: `MovementController_Beh.thy`:173

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: No subgoals!

**Raw**
```
*** No subgoals!
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:173
```

**Java trace**
  - Java: `MovementController_Beh.thy`:173

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: At command "by" (line 173 of "/Users/ranwei/Git/Forge/forge.transformations/outp

**Raw**
```
*** At command "by" (line 173 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy")
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/MovementController_Beh.thy:173
```

**Java trace**
  - Java: `MovementController_Beh.thy`:173

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_parse_error — Theory will not parse

**Raw**
```
*** Outer syntax error (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy"): name expected,
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:31
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:31

**Fix directive**
Isabelle's outer syntax parser rejected the generated .thy file. The line number above points at the offending construct. This is usually a template-side bug — see docs/fixes/I2_template_fork.md for known classes.

### Issue 6: isabelle_other — Isabelle error: but keyword update (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/ou

**Raw**
```
*** but keyword update (line 31 of "/Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy") was found
At: /Users/ranwei/Git/Forge/forge.transformations/output/isabelle/GasAnalysisController_Beh.thy:31
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:31

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_parse_error — Theory will not parse

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
(none identified)

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
