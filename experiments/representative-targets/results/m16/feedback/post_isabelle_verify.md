# Isabelle Verification — FAILED

## Summary
Isabelle: exit 139, 1 theory marker(s) seen before failure.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle reported a failure but no error lines parsed

**Raw**
```
Started at Mon Nov 9 18:08:24 GMT 2026 (polyml-5.9_x86_64_32-linux on f7f5ce1d8fb9)
ISABELLE_TOOL_JAVA_OPTIONS="-Djava.awt.headless=true -Xms512m -Xmx4g -Xss16m"
ISABELLE_BUILD_OPTIONS=""

ML_PLATFORM="x86_64_32-linux"
ML_HOME="/opt/isabelle/contrib/polyml-219e0a248f70/x86_64_32-linux"
ML_SYSTEM="polyml-5.9"
ML_OPTIONS="--maxheap 6g"

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
Session Unsorted/SynController_Check
Running SynController_Check ...
SynController_Check: theory SynController_Check.SynController_Beh
/tmp/isabelle-/bash_script6224214485036977204: line 1:   191 Segmentation fault      "$POLYML_EXE" -q --maxheap 6g --gcthreads 0 --exportstats --eval \(PolyML.SaveState.loadHierarchy\ \[\"/opt/isabelle/heaps/polyml-5.9_x86_64_32-linux/Pure\",\ \"/opt/isabelle/heaps/polyml-5.9_x86_64_32-linux/HOL\",\ \"/opt/isabelle/heaps/polyml-5.9_x86_64_32-linux/HOL-Eisbach\",\ \"/opt/isabelle/heaps/polyml-5.9_x86_64_32-lin
```

**Fix directive**
The runner exited non-zero but the output didn't match any known error pattern. Read the raw text above; consider extending the classifier in forge_pipeline/feedback/isabelle.py if this becomes common.

## Files to review
- SynController.java
- SynMode.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
