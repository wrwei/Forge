# Steam Boiler, pass 4, iteration 1

## What I wrote

15 Java files under `steam_boiler/`:

- **`SteamBoilerController`**: five modes and 38 transitions, all triggered by one per-cycle `Transmission` event (SB-V3), so no guard-only cycles exist. Guards within a mode are mutually exclusive and ordered emergency, rescue, degraded, regulation (SB-FR8). Each transition sends MODE(m). State: q, v, p, the estimate bounds, and the init flags.
- **`BoilerSensor`**, with `UnitRecord`, `PumpUnit` and `UnitStatus`: decodes transmissions and judges failures (SB-FR1–FR5). It tracks unit status and acknowledgements (SB-V9), counts successive STOPs (SB-V10) and composes the failure and repair messages (SB-FR6/7).
- **`BoilerActuator`**: collects the outgoing transmission. **`SteamBoilerProgram`**: runs each cycle.
- **Events and types**: `InputEvent` and `OutputEvent` (sealed), `ModeValue`, `PumpReading`, `PumpControlReading` and `BoilerConstants`.

The trace has 221 entries covering all 53 requirements.

## Decisions the requirements left open

- **Constants**: invented C=1000, M1=150, M2=850, N1=400, N2=600, W=10, U1=U2=2, P=5.
- **Regulation**: all four pumps on below N1, all off above N2. Rescue uses the midpoint of the estimate.
- **Risk**: judged on the next cycle's worst case. The estimate uses known-pouring pumps, which respects start-up latency.
- **Untrustworthy estimate**: a level failure while the steam device or a pump controller is out goes directly to emergency stop.
- **"Pump-controller units fail"**: read as any one controller failing.
- **Aberrant messages**: read strictly, e.g. a late PHYSICAL_UNITS_READY, an unexpected acknowledgement, or a repair notice for a sound unit.
- **Start-up**: pumps assumed off. A missing reading defaults to 0.
- **Emergency stop**: terminal.

## Unresolved

The per-unit protocol (SB-FR6/7, SB-V9) is outside the controller's state machine, so the model abstracts it. Model state includes five real variables whose CSP ranges I cannot predict.
