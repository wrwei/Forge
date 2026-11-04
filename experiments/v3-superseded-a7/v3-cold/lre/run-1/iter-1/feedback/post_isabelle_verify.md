# Isabelle Verification — FAILED

## Summary
Isabelle: exit 125, 1 theory marker(s) seen before failure.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle reported a failure but no error lines parsed

**Raw**
```
Started at Thu Oct 29 11:43:33 GMT 2026 (polyml-5.9_x86_64_32-linux on 98fce9e3d516)
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
Session Unsorted/LreController_Check
Running LreController_Check ...
LreController_Check: theory LreController_Check.LreController_Beh
time="2026-10-29T11:45:00Z" level=error msg="error waiting for container: unexpected EOF"
## Obligation inventory

- Obligations in the session: **47** across 1 theory
- `LreController_Beh`: 47 obligations (23 invariant-preservation, 23 requirement-preservation (anonymous), 1 deadlock-freedom)

Isabelle aborts at the first failing proof, and the failure is in `LreController_deadlock_free`. **The obligations
```

**Fix directive**
The runner exited non-zero but the output didn't match any known error pattern. Read the raw text above; consider extending the classifier in forge.dashboard/web/feedback/isabelle.py if this becomes common.

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
