# post_codegen — chemical_detector (iteration 1)

**Status:** uncertain

Initial cold codegen for chemical_detector: 19 files, two controllers (GasAnalysisController, MovementController) plus a ChemicalDetector driver. All 81 requirements are traced in result_codegen.json. The flagged items are design choices and invented defaults; none is known to block the pipeline.

## Issues

### 1. [design_choice] Gas-analysis 'final state' realised as SOURCE_FOUND -> CONCLUDED (no Final mode)

CD-GA-Beh6 ends gas analysis in a final state after emitting stop. This controller is the one whose Isabelle theory is generated (CLAUDE.md: no Final mode allowed), and every mode needs an outgoing transition. A self-loop on the state entered with `stop` would re-run its lifted entry and re-send stop on every loop, so a two-step terminal is used: GAS_DETECTED -[ins>=thr]/stop-> SOURCE_FOUND -> CONCLUDED. CONCLUDED absorbs further gas readings (storing gs, per CD-GA-Var1) without classifying them, so no further turn/stop/resume is ever emitted.

**Fix directive:** Confirm that an idle, gas-absorbing CONCLUDED state is an acceptable reading of 'concludes its search, processing no further readings'.

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:61-76` (step) — CD-GA-Beh6, CD-GA-FR4

### 2. [design_choice] Movement Found -> HALTED terminal with a stop-triggered self-loop

CD-MV-Beh9 (Found_to_Final). FOUND emits flag and halts on entry, then moves autonomously to HALTED. HALTED keeps a stop-triggered self-loop, so it has an outgoing transition without re-running Found's entry (a single flag).

**Fix directive:** Confirm HALTED + stop self-loop as the realisation of 'Once in Found the movement subsystem stays halted'.

- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:142-148` (step) — CD-MV-Beh9, CD-MV-FR3

### 3. [invented_default] Constant values are not given by the requirements

thr=3.0, lv=1.0, evadeTime=1, stuckPeriod=3, stuckDist=1.0, outPeriod=2 (integer-valued, small, to keep the CSP model finite and small). The requirements say these are 'configured at system startup' without giving values.

**Fix directive:** Replace with deployment values if known.

- `java.generated.project/src/main/java/chemical_detector/constants/ChemConstants.java:12-32` (ChemConstants) — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 4. [invented_default] Meaning of 'indicates the target chemical', target identity, and empty-reading defaults

analysis returns gasD iff some sensor reports the configured target Chem (by id, passed to GasFunctions) with intensity > 0. intensity/location return 0.0/Front on an empty reading (their stated precondition is non-empty); location breaks ties towards the lowest position.

**Fix directive:** Confirm the classification criterion and tie-breaking.

- `java.generated.project/src/main/java/chemical_detector/sensor/GasFunctions.java:30-73` (analysis) — CD-Fn1, CD-Fn2, CD-Fn3

### 5. [invented_default] Sensor position to Angle mapping (angle(x))

CD-Fn3 uses angle(x) but does not define it. Positions cycle Front, Right, Back, Left (1->Front, 2->Right, 3->Back, 4->Left, 5->Front, ...).

**Fix directive:** Replace with the real sensor-array geometry.

- `java.generated.project/src/main/java/chemical_detector/sensor/GasFunctions.java:84-98` (angle) — CD-Fn3, CD-DM7

### 6. [ambiguous_requirement] intensity() is the peak over all sensors, not only the target chemical

CD-Fn2 defines intensity as the maximum i over the whole sequence, while analysis is about the target chemical only. Implemented literally (all sensors), so a strong non-target reading can drive the threshold check and the turn direction.

**Fix directive:** Confirm whether intensity/location should be restricted to the target chemical.

- `java.generated.project/src/main/java/chemical_detector/sensor/GasFunctions.java:45-57` (intensity) — CD-Fn2, CD-Fn3

### 7. [design_choice] Threshold guard written as `ins >= THR`, not a goreq(...) call

CD-Fn4 says the threshold check uses goreq. A guard predicate whose initialiser is a bare method call becomes an unconstrained boolean in the extracted model, so it would lose the link to ins/thr. The guard uses the primitive comparison, which is exactly goreq's definition. goreq itself is implemented and used by intensity/location.

**Fix directive:** None unless the model must literally reference goreq.

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:40-41` (step) — CD-Fn4, CD-GA-Beh6, CD-GA-Beh7

### 8. [design_choice] Second outgoing branch of Analysis / AvoidingAgain written as `else`

Analysis: `sts == noGas` -> NO_GAS, else -> GAS_DETECTED (equivalent to sts == gasD because Status has exactly two values). AvoidingAgain: progress -> AVOIDING, else -> GETTING_OUT (the requirement's stuck condition is exactly the negation of progress). The else form makes the guard pair an explicit P / not P total cover.

**Fix directive:** None.

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:51-57` (step) — CD-GA-Beh4, CD-GA-Beh5
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:118-128` (step) — CD-MV-Beh17, CD-MV-Beh18

### 9. [design_choice] Waiting's during-action randomWalk() and the wait() actions

randomWalk() is called at the head of the WAITING block on every cycle (during semantics in Java; the extractor records it as Waiting's entry). wait(evadeTime)/wait(outPeriod) are Vehicle.pause(n) requests: the step() method never blocks.

**Fix directive:** None.

- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:46-46` (step) — CD-MV-FR1, CD-OP2
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:78-78` (step) — CD-MV-FR4, CD-Const3
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:127-127` (step) — CD-MV-FR7, CD-Const6

### 10. [design_choice] Operations are Vehicle methods / a ChangeDirection class, not compute() classes

move, randomWalk and shortRandomWalk are platform operations (CD-OP1..3 say 'provided by the Vehicle') and have no computed result, so they are Vehicle methods. changeDirection(l) (CD-OP4) has conditional behaviour, which a compute() body (assignments only) cannot express, so it is a ChangeDirection class calling Vehicle.move. The extractor emits move as an operation call and changeDirection as an output event.

**Fix directive:** None unless compute()-style operation classes are mandatory.

- `java.generated.project/src/main/java/chemical_detector/operation/ChangeDirection.java:21-29` (changeDirection) — CD-OP4
- `java.generated.project/src/main/java/chemical_detector/actuator/Vehicle.java:26-31` (move) — CD-OP1, CD-OP2, CD-OP3

### 11. [scope_question] A turn arriving while in AvoidingAgain is dropped by the Java

AvoidingAgain has no turn transition. In the model the sender blocks until AvoidingAgain is left, which happens autonomously on the next cycle. In the Java step model, a turn delivered in that cycle is consumed without updating `a`, and the next gas reading re-issues a turn.

**Fix directive:** Decide whether AvoidingAgain should record `a` on turn.

- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:111-128` (step) — CD-MV-Var1, CD-MV-FR6

### 12. [design_choice] Chem modelled as record Chem(int id)

Chem is an opaque identity with equality only; a single-nat record gives value equality without inventing chemical names.

**Fix directive:** None.

- `java.generated.project/src/main/java/chemical_detector/domain/Chem.java:8-9` (Chem) — CD-DM4

## Next step

Run the pipeline (compile -> coverage -> preflight -> t2m -> m2m -> m2t -> verifiers).
