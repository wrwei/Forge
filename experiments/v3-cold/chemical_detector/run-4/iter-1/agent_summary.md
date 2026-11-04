# Iteration summary: chemical_detector, cold pass 4

**What was written.** 16 Java files under `chemical_detector/`, plus `result_codegen.json` (150 entries covering all 81 requirements). There are two controllers:

- `GasAnalysisController`: Reading, Analysis, NoGas, GasDetected, Concluded.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Halted.

`VehicleSensor` holds analysis, intensity, location, goreq and the odometer. `Clock` provides T. `Actuator` provides move, randomWalk, shortRandomWalk, changeDirection and pause (becomes wait), and sends events. turn, stop and resume exist as both OutputEvent and InputEvent records, so each becomes one shared event.

**Decisions the requirements left open:**
- **Constants:** THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. All are whole numbers, so model rounding does not change them. HALT_VELOCITY=0.0 was added.
- **Detection:** a reading indicates gas when some value has the target Chem with i > 0.
- **Sensor layout:** angle(x) maps positions 1–4 to Left, Right, Back, Front, repeating; on a tie the lowest position wins.
- **Types:** Intensity is real; Chem is a record with a nat id.
- **randomWalk:** Waiting's during action is issued on every entry to Waiting.
- **Odometer:** a sensor variable, not an event.
- **Vehicle operations:** Actuator calls, not compute() classes, so the model omits changeDirection's steering rule.
- **Final states:** gas analysis ends in Concluded. Found→Halted and NoGas→Reading have no trigger and no guard; neither can repeat in a loop of internal steps.

**Unresolved:**
- intensity and location range over all chemicals, as specified; whether only the target chemical should count is open.
- Some names differ only in case: Analysis/analysis, NoGas/noGas, Angle/Loc values.
- No Java class routes events between the controllers.

All of this is recorded in `post_codegen.md`. The project compiles, and a scratchpad smoke test exercised every path.
