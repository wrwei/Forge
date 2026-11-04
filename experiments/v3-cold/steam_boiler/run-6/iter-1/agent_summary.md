# Steam Boiler, pass 6, iteration 1

## What I wrote

Fifteen Java files in `steam_boiler`:

- **Types and events:** `OperatingMode` (five modes, INITIALIZATION first), `ModeCode`, `PumpId`, pump payload records, and the `InputEvent` and `OutputEvent` hierarchies.
- **`BoilerConstants`.**
- **`BoilerSensor`:** reception and analysis, using `PumpUnit` and `UnitStatus`.
- **`BoilerActuator`.**
- **`SteamBoilerController`:** stateless apart from `currentMode`, with 29 `Cycle`-triggered transitions. Each mode's guards exclude one another and cover every case; emergency stop is terminal.
- **`SteamBoilerProgram`:** runs each cycle as receive, analyse, step, transmit.

Why: guards may use only sensor calls, so history lives in the sensor and the model's only state variable is the mode.

## Decisions the requirements left open

- **Constants (invented):** C=1000, M1=100, N1=400, N2=600, M2=900, W=20, U1=U2=2, P=10.
- **Regulation:** open all four pumps below N1, close all four above N2. A mode-changing cycle sends only MODE.
- **Initialization:** pump 1 fills; the valve drains. Once the level is in range, both close and PROGRAM_READY repeats until PHYSICAL_UNITS_READY.
- **Rescue exit (SB-Beh21):** any failed pump controller forces emergency stop.
- **Level failure** while the steam device or a pump controller is already failed goes straight to emergency stop (SB-FR8).
- **Aberrant messages:** missing or duplicated data, unsolicited failure acknowledgements, start-up messages outside initialization, or PHYSICAL_UNITS_READY before PROGRAM_READY.
- **Estimate:** lower and upper bounds from W, U1, U2 and pump-controller observations, with no tolerance.
- **Emergency stop:** MODE(EMERGENCY_STOP) is sent once, then the program stops.

## Unresolved

- Failure announcements and repair acknowledgements come from the sensor's bookkeeping in the transmit stage. No model transition emits them.
- Rescue lasts only a few cycles with these constants.
- Untested: whether the Isabelle Z-Machine treats the terminal emergency-stop mode as an end state.

Details are in `post_codegen.md`.
