# ITER_SUMMARY: chemical_detector, run 3

## Iteration 1

**What I wrote.** I wrote 18 files under `chemical_detector/` and the trace, which covers all 81 requirements in 161 entries. There are two controllers, one per reasoning subsystem.
- `GasAnalysisController` has modes READING, ANALYSING, NO_GAS, GAS_DETECTED and CONCLUDED.
- `MovementController` has modes WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN and GETTING_OUT.

Gas analysis sends `OutputEvent.Turn/Stop/Resume`, and movement receives `InputEvent.Turn/Stop/Resume` as shared events. The functions analysis, intensity, location and goreq are on `GasSensorArray`. The odometer is a zero-argument sensor read. `Clock` (`now`, `delay`) gives the evasion clock and the waits. The movement operations are methods of `VehicleActuator`.

**Decisions the requirements did not settle:**
- **Constants:** THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2. They are small and integer-valued, and STUCK_PERIOD > EVADE_TIME.
- **"Indicates the target":** a value indicates the target chemical when its `c` equals the target Chem, which is configured at startup, and its `i` > 0.
- **Sensing directions:** reading positions map 1→Front, 2→Right, 3→Back, 4→Left, repeating. On a tie the first position wins.
- **Empty reading:** intensity returns 0.0 and location returns Front.
- **Final states:** the final state of gas analysis is a terminal mode, CONCLUDED. FOUND is terminal.
- **During action:** Waiting's during action, randomWalk(), is issued on every entry to Waiting and in the constructor.
- **Operations:** CD-OP1..4 are actuator methods, not compute() classes, because they are parameterised commands with no result.
- **Naming:** the Analysis state is called ANALYSING, so that it does not differ only in case from the `analysis` function.
- **Types:** Intensity is real. Chem is a record with a nat id.

**Unresolved.** No Java class forwards gas analysis's outputs to movement. Delivery is left to the deployment harness, and in the model the module connects the shared events. Full list: `post_codegen.md`.
