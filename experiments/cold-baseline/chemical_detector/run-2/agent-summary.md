# Chemical Detector — Cold Baseline Run 2

Single-shot generation, no prior context or feedback.

## Layout

- `chemdetector.annotation` — `@RoboChartType`, `@Clock`, `@SensorService`, `@RoboChartWait`.
- `chemdetector.data` — `Status`, `Angle`, `Loc`, `Chem`, `Intensity`, `GasSensor`.
- `chemdetector.constants.Constants` — `thr`, `lv`, `evadeTime`, `stuckPeriod`, `stuckDist`, `outPeriod`.
- `chemdetector.vehicle.Vehicle` — `move`, `randomWalk`, `shortRandomWalk`, `changeDirection`, `pause` (`@RoboChartWait`).
- `chemdetector.sensor.GasFunctions` — `analysis`, `intensity`, `location`, `angle`, `goreq` (RoboChart functions).
- `chemdetector.clock.StuckClock` — `@Clock`; `nowMs`/`reset`/`since`.
- `chemdetector.gasanalysis` — controller + events + `GAMode` (Reading / Analysis / NoGas / GasDetected / Final).
- `chemdetector.movement` — controller + events + `MVMode` (Waiting / Going / Avoiding / TryingAgain / AvoidingAgain / GettingOut / Found / Final).
- `chemdetector.ChemicalDetectorSystem` — top-level wiring and inter-controller routing.

## Design choices

- Two controllers using the single-method, mode-nested if-else pattern.
- Each guard surfaced as a named boolean predicate, no ternaries, no sentinel checks.
- Shared events (`turn`, `stop`, `resume`) modelled as a `gasanalysis.OutputEvent` sealed interface and a matching variant inside `movement.InputEvent`; routing is explicit.
- Stuck-detection compares `clock.nowMs() - tStart` against `stuckPeriod` (the ETL rewrites this to `since(tStart)`).
- `Found` -> `Final` made an autonomous transition (per CD-MV-Beh9); entry actions for `Found` are inlined wherever the transition fires.
- Index-to-angle map in `GasFunctions.angle` chosen as Front/Right/Back/Left at positions 0..3 (system description does not specify this; documented invented default).

## Caveats

- `Chem` modelled as an int wrapper; `Intensity` as a double wrapper.
- Threshold and velocity values are illustrative defaults.
