# ITER_SUMMARY — steam_boiler, run 2

## Iteration 1

**What was written.** 13 Java files under `steam_boiler/` (no Java was present beforehand):
`SteamBoilerController` (mode enum `BoilerMode`; 39 transitions; state variables `plantWaiting`,
`valveOpen`, `programReadySent`), `BoilerSensor` (+ `UnitStatus`, `PumpMonitor`), `BoilerActuator`,
`ControlCycle`, `BoilerConstants`, events `InputEvent`/`OutputEvent`, enums `OperatingMode`/`PumpId`,
`RoboChartType`. Trace: 226 entries, all 53 requirements.

**Why this shape.** All of a cycle's messages arrive simultaneously, so the controller is stepped
once per cycle on a single event `MessagesReceived`, and guards read sensor queries. The physics
and per-unit bookkeeping (failure detection FR1–FR5, unit status, STOP count, level bounds) sit in
the sensor, so the extracted model stays small (mode plus three booleans). Guards in each mode are
mutually exclusive and exhaustive, ordered emergency > rescue > degraded (SB-FR8).

**Decisions the requirements left open.**
- Constant values invented (C=1000, M1=150, M2=800, N1=400, N2=600, W=25, U1=U2=4, P=10), chosen
  to satisfy SB-DM1's danger conditions; all integral.
- Level risk = worst-case reachable level within the next cycle crosses M1/M2, counting pump latency.
- Regulation: all pumps on below N1, off above N2; init fills with all four pumps.
- "Pump-controller units fail" in rescue = any controller defective.
- STOP and transmission failure apply even before STEAM_BOILER_WAITING.
- Aberrant messages: init handshake messages out of place, duplicated data, acknowledgements or
  repairs that do not match the unit status.
- Failure announcements and repair acknowledgements are produced by the sensor, outside the
  state machine.
- Emergency stop is terminal (an empty mode block), so MODE is not repeated after it.

**Unresolved.** Nothing blocking. Full list in `post_codegen.md`.
