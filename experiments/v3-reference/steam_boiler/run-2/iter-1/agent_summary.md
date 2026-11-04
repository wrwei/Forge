# Iteration 1 — steam_boiler, run 2

No Java was present before writing.

**What I wrote.** Package `steam_boiler`:
- `SteamBoilerController`: 5 modes. Every transition is triggered by the per-cycle `InputEvent.Transmission`. Each mode's guards are explicitly disjoint and exhaustive, and EMERGENCY_STOP is terminal.
- `BoilerSensor`: the facts the controller reads in a cycle.
- `UnitMonitor`: message reception, failure detection (FR1–FR5), unit status and pending acknowledgements, the STOP count, the level estimate and predictions, and the FR6/FR7 messages.
- `BoilerActuator`: the outgoing transmission.
- `SteamBoilerProgram`: the receive → analyse → transmit cycle.
- Enums `OperatingMode`, `ModeValue` (MODE payload) and `WaterFlow`; the constants; the sealed `InputEvent` and `OutputEvent` types.

**Why this split.** The profile allows no conditional actions, and every controller field becomes a model state variable. Per-unit bookkeeping in the controller would explode the model's branches and states. The controller keeps the mode logic, the initialisation sub-phases and the valve.

**Decisions the requirements did not settle:**
- Constants are invented: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10.
- Pumps are assumed closed before any order.
- All four pumps switch together. Regulation is predictive (worst-case level before the next transmission), so the start-up latency is anticipated.
- The M1/M2 risk is judged with the pumps as this cycle leaves them.
- Initialisation follows Beh4/5/7 on the reading, opens all pumps to fill, and closes the valve on abort.
- Emergency stop sends MODE once and then stops (Beh22 over Beh24).
- "Aberrant" messages are enumerated.
- The level-dynamics check is skipped during initialisation, because the valve's rate is unknown.
- Level failure together with a steam or pump-controller failure goes to rescue first (FR8, read literally).

**Unresolved.** FDR, Dafny and Isabelle do not check the monitor's logic (it is outside the model); a scratch simulation behaved sensibly. Details: `post_codegen.md`.
