# Iteration 3 — steam_boiler, run 2

**Iteration 2 result.** Preflight and vacuity now pass. FDR4 was again killed at about 80 GB. Isabelle's `deadlock_free` timed out; its report says the `St.exhaust_disc` step needs every mode to have an event-only transition, and mine all carried guards.

**Changes (Java behaviour kept, re-checked by simulation):**
- The unit monitor's `analyse()` now classifies each cycle's transmission into one input event, in order of precedence: `StopConfirmed` (STOP ×3), `TransmissionFault`, `LevelRisk` (outside initialisation), `SteamBoilerWaiting` (first arrival), `PhysicalUnitsReady` (legitimate handshake answer), `Transmission`.
- The first three are unguarded transitions to emergency stop in every mode where they apply, so every mode now has an event-only transition.
- The boolean sensor facts drop from 13 to 8, which shrinks the state space the model checkers explore.
- 42 transitions; for each event, a mode's guards are mutually exclusive (checked over all assignments).

**Decisions the requirements leave open** (unchanged):
- invented constants: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10;
- pumps are assumed closed before any order;
- all four pumps switch together, with predictive regulation;
- initialisation fills with all pumps, starts its checks the cycle after STEAM_BOILER_WAITING, and closes the valve before filling or announcing;
- emergency stop is terminal (sends MODE once);
- "aberrant" messages are enumerated;
- no level-dynamics check during initialisation (valve rate unknown);
- under FR8, a level failure goes to rescue before a steam or pump-controller emergency.

**Unresolved.** Detection, bookkeeping and the protocol are in `UnitMonitor`, outside the extracted model, so the verifiers do not check them. If FDR4 still exhausts memory, the remaining reductions would change behaviour or merge requirement conditions; I will weigh them against the report.
