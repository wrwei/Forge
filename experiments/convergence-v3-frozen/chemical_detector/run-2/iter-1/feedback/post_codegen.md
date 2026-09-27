# Phase 2 — Interactive codegen review (chemical_detector, run 2, iteration 1)

**status:** uncertain — 7 item(s) for review

Cold generation from `requirement_all.json` and `system_description.txt`. All 81
requirements are traced in `java.generated.project/result_codegen.json`; 16 Java
files under `src/main/java/chemical_detector/`. Two state machines:
`GasAnalysisController` (Reading, Analysis, NoGas, GasDetected, Stopped) and
`MovementController` (Waiting, Going, Found, Avoiding, TryingAgain,
AvoidingAgain, GettingOut). They communicate over one `SystemEvent` bus via
turn / stop / resume; the movement machine emits flag.

## next_step

Run the pipeline (compile → coverage → preflight → T2M → M2M → M2T → verifiers).

## Issues

### 1. Chem is an enum with a designated target species (`invented_default`)

**Requirements:** CD-DM4, CD-Fn1

CD-DM4 calls Chem an opaque type with equality only; CD-Fn1 needs to know which species is 'the target chemical' but no requirement names a target constant.

**fix_directive:** Confirm that modelling Chem as enum {NoChem, TargetChem} and defining analysis(gs)=gasD iff some reading carries Chem.TargetChem is the intended reading of CD-Fn1. If the target species should instead be a configured constant, add it to CD-Const*.

### 2. Intensity is a plain double, compared with >= at the guard (`design_choice`)

**Requirements:** CD-DM5, CD-Fn4

CD-DM5 asks only for a total order; CD-Fn4 defines goreq. goreq is implemented on VehicleSensor and used inside intensity(), but the GasDetected guard uses `ins >= thr` directly rather than goreq(ins, thr).

**fix_directive:** A two-argument extracted RoboChart function applied inside a guard risks the CSP generator's unsupported-callee path, so the threshold test is written as a primitive comparison. Confirm this is acceptable fidelity for CD-Fn4.

### 3. CD-OP1..OP4 realised as Vehicle methods, not operation/ classes with compute() (`design_choice`)

**Requirements:** CD-OP1, CD-OP2, CD-OP3, CD-OP4

chain_of_thought_codegen.txt asks for one class per types:["operation"] requirement with a compute() method. CD-OP1..OP4 are platform operations the Vehicle provides, not derived computations.

**fix_directive:** move/randomWalk/shortRandomWalk/changeDirection are emitted as Vehicle methods; the two-argument calls become RoboChart LOperations calls and the zero-argument calls become platform events. Confirm no separate Operation definitions are wanted.

### 4. Tick self-loops on the two terminal modes, and a Stopped mode instead of Final (`invented_default`)

**Requirements:** CD-DC1, CD-GA-Beh6, CD-MV-Beh9

CD-GA-Beh6 and CD-MV-Beh9 specify terminal behaviour. No requirement asks for a Tick event.

**fix_directive:** java_codegen_rules.txt requires every mode to have an outgoing transition, so GasAnalysisMode.Stopped and MovementMode.Found each carry a Tick-triggered self-loop, and the gas-analysis terminal mode is named Stopped rather than Final. Both are instrument accommodations, not requirements. Review whether they should be recorded as a deviation.

### 5. CD-Evt3 (odometer) modelled as a sensor query, not a consumed event (`ambiguous_requirement`)

**Requirements:** CD-Evt3, CD-MV-Var2, CD-MV-Var3

CD-Evt3 is listed among the events but describes sampling ('the movement subsystem samples this distance into d0 or d1'), which is a read, not an event consumption.

**fix_directive:** odometer() is a zero-argument VehicleSensor query read in the Avoiding / AvoidingAgain entry actions, so it becomes a RoboChart Sensors variable rather than a trigger. Confirm this reading of CD-Evt3.

### 6. All six configuration constants set to 1 (`invented_default`)

**Requirements:** CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

CD-Const1..6 say the values are configured at startup but give none.

**fix_directive:** thr=1.0, lv=1.0, evadeTime=1, stuckPeriod=1, stuckDist=1.0, outPeriod=1 — chosen integral and inside the dashboard's 0..1 CSP type ranges so no constant is ceiled and no guard is trivially false in the checked model. Substitute real deployment values if they matter.

### 7. Waiting's randomWalk() is an entry action, not a during action (`design_choice`)

**Requirements:** CD-MV-FR1, CD-OP2

CD-MV-FR1 specifies randomWalk() as the during action of Waiting.

**fix_directive:** vehicle.randomWalk() sits at the head of the Waiting mode block, which the extractor lifts to a RoboChart state entry action. RoboChart during actions have no counterpart in the single-method step() shape the extractor requires.
