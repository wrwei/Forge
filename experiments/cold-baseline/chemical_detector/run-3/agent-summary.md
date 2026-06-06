# Cold-baseline run-3: Chemical Detector

Single-shot cold codegen. No prior session, no verifier feedback.

## Architecture

Two state machines per CD-ARCH2:

- **GasAnalysisController** (`Reading`, `Analysis`, `NoGas`, `GasDetected`,
  `Final`) — classifies readings via `GasFunctions.analysis`, gates the
  threshold via `goreq`, and emits the shared events `turn`, `stop`,
  `resume` on a `SharedEventBus`.
- **MovementController** (`Waiting`, `Going`, `Found`, `Avoiding`,
  `TryingAgain`, `AvoidingAgain`, `GettingOut`, `Final`) — drives the
  Vehicle (`move`, `randomWalk`, `shortRandomWalk`, `changeDirection`,
  `pause`, `sendFlag`), consumes `Obstacle`/`Odometer` from the Vehicle
  and `Turn`/`Stop`/`Resume` from the GA controller.

Clock `T` is realised by capturing `clock.nowMs()` into `tickT` and
using `clock.nowMs() - tickT` predicates (rewritten to `since(tickT)`).

## Pragma decisions

- `Intensity` collapsed to plain `double` so `Constants.THR` can be a
  numeric constant (CSP-gen v3 doesn't emit record-typed constants).
- Bare-precondition cover ensured for every non-final state: `Analysis`
  and `GasDetected` use an `else` fallback (binary predicates); all MV
  states have unguarded event-triggered branches.
- `@RoboChartWait` on `Vehicle.pause(int)` for the evade/out periods.
- `@SensorService` on both `Vehicle` and `GasFunctions`; `@Clock` on the
  `Clock` class.
