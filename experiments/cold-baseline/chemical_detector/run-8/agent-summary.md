# Chemical Detector Baseline — Run 8

Single-shot, cold-baseline Java implementation. No prior session, no
verifier feedback.

## Structure

- `annotation/` — `@RoboChartType`, `@Clock`, `@SensorService`,
  `@RoboChartWait` markers.
- `datatype/` — `Status`, `Angle`, `Loc` enums; `Chem`, `Intensity`,
  `GasSensor` records.
- `event/` — sealed `InputEvent` (Gas/Obstacle/Odometer/Turn/Stop/
  Resume/Tick) and `OutputEvent` (Turn/Stop/Resume/Flag) hierarchies.
- `constants/` — `Constants` (thr, lv, evadeTime, stuckPeriod,
  stuckDist, outPeriod).
- `sensor/` — `Sensor` (analysis/intensity/location/goreq/angle);
  `SystemClock` (millisecond tick for since(T)).
- `actuator/` — `Actuator` event queue.
- `vehicle/` — `Vehicle` (move/randomWalk/shortRandomWalk/pause/flag).
- `operation/` — `ChangeDirection` (compute() with only this.field =
  expr assignments).
- `controller/gas/` — `GasMode` (Reading/Analysis/NoGas/GasDetected/
  Final) and `GasAnalysisController`.
- `controller/movement/` — `MovementMode` (Waiting/Going/Found/
  Avoiding/TryingAgain/AvoidingAgain/GettingOut/Final) and
  `MovementController`.
- `ChemicalDetectorSystem` — top-level wiring; routes gas-analysis
  output events into movement-controller inputs.

## Conformance notes

- Two single-method mode-nested state machines; outer branches always
  `currentMode == X`.
- Named boolean predicates declared before each if-else chain; no
  ternaries, no sentinel guards.
- `compute()` in `ChangeDirection` uses only `this.field = expr`.
- No lambdas/streams/method-refs/pattern-matching instanceof.
- Stop/Resume duplicated as high-priority inner branches in every
  applicable movement-controller mode block.
