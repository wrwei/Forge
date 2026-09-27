# Isabelle Verification — FAILED

## Summary
Isabelle: exit 137, 2 theory marker(s) seen before failure.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Tactic timeout, suspected in MovementController_deadlock_free (deadlock_free — inferred from .thy)
- Tactic interrupt — cascade from timeout in MovementController_deadlock_free

## Issues
### Issue 1: isabelle_other — Isabelle reported a failure but no error lines parsed

**Raw**
```
Started at Fri Sep 25 15:18:00 GMT 2026 (polyml-5.9_x86_64_32-linux on 184515892c8a)
ISABELLE_TOOL_JAVA_OPTIONS="-Djava.awt.headless=true -Xms512m -Xmx4g -Xss16m"
ISABELLE_BUILD_OPTIONS=""

ML_PLATFORM="x86_64_32-linux"
ML_HOME="/opt/isabelle/contrib/polyml-219e0a248f70/x86_64_32-linux"
ML_SYSTEM="polyml-5.9"
ML_OPTIONS="--minheap 500"

Session Pure/Pure
Session FOL/FOL
Session Misc/Tools
Session HOL/HOL (main)
Session Unsorted/Explorer
Session HOL/HOL-Library (main timing)
Session HOL/HOL-Combinatorics (main timing)
Session HOL/HOL-Computational_Algebra (main timing)
Session HOL/HOL-Analysis (main timing)
Session HOL/HOL-Eisbach
Session AFP/Optics (AFP)
Session Unsorted/Z_Toolkit
Session Unsorted/Shallow-Expressions
Session Unsorted/Abstract_Prog_Syntax
Session Unsorted/Shallow-Expressions-Z
Session Unsorted/Interaction_Trees
Session Unsorted/ITree_Simulation
Session Unsorted/ITree_UTP
Session Unsorted/ITree_VCG
Session Unsorted/Z_Machines
Session Unsorted/GasAnalysisController_Check
Running GasAnalysisController_Check ...
GasAnalysisController_Check: theory GasAnalysisController_Check.MovementController_Beh
GasAnalysisController_Check: theory GasAnalysisController_Check.GasAnalysisController_Beh
/tmp/isabelle-/bash_script10061148343971078703: line 1:   197 Killed                  "$POLYML_EXE" -q --minheap 500 --gcthreads 0 --exportstats --eval \(PolyML.SaveState.loadHierarchy\ \[\"/opt/isabelle/heaps/polyml-5.9_x86_64_32-linux/Pure\",\ \"/opt/isabelle/heaps/polyml-5.9_x86_
```

**Fix directive**
The runner exited non-zero but the output didn't match any known error pattern. Read the raw text above; consider extending the classifier in forge.dashboard/web/feedback/isabelle.py if this becomes common.

## Files to review
(none identified)

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
