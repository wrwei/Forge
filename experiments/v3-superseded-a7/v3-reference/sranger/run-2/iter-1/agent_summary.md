# SRanger, run 2: iteration summary

## Iteration 1

**Written.** Nine files in `sranger`:
- `RoboChartType`
- events `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`)
- `Sensor` (`distance()`)
- `Actuator` (`apply`, last linear/angular velocity)
- `SRangerMode` (`MOVING` first, `TURNING`, `FINAL`)
- `SRangerConstants`
- `Clock`
- `SRangerController`

The controller has one `step()` with predicates `obstacleDetected` and `turnDurationElapsed`, three mode blocks, six transitions (SR-Beh2–7), and `currentMode = MOVING` (SR-Beh1). `result_codegen.json` traces all 23 requirements; `post_codegen` lists the details. No Java existed beforehand.

**Decisions the requirements left open:**
- No-reading distance: 1000.0 m. A negative reading also counts as no reading.
- `clockResetTime` starts at 0.0. It is never read before Turning resets it.
- The constructor issues the initial `Move(moveVel, 0)`, Moving's power-up entry action.
- Moving→Turning needs the `obstacle` event and `obstacleDetected` (`<=`, per SR-GP1). An obstacle event above the threshold is ignored.
- Turning order: `endTask`, then elapsed time, then `tick && !turnDurationElapsed`. With tick first, a tick-driven controller could never leave Turning.
- Tick self-loops do nothing (SR-Beh4/7). There is no re-entry action and no clock reset.
- Time comes from an externally advanced `Clock`, held in the field `timer` (`clock` is reserved).
- `Move` carries two real components, as SR-DM4 states. The actuator method is `apply`, so it cannot clash with `Move`.
- Final is terminal: its block is empty.

**Unresolved:** `OBSTACLE_THRESHOLD = 0.5` becomes 1 in the model's constants interface, so model-level results concern a 1 m threshold. I kept the specified value and unit.
