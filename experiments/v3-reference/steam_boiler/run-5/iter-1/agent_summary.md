# Steam Boiler — iteration 1

**Written.** 18 Java files under `steam_boiler/`. `SteamBoilerController.step` holds the five-mode machine. Every transition is triggered by `TransmissionReceived`, sent once per cycle after `BoilerSensor.receive` has taken the whole transmission (SB-V3). Each mode's guards are exclusive and cover every case, so no mode gets stuck and no event-less cycle exists. `EMERGENCY_STOP` is terminal. `BoilerSensor` does the per-cycle analysis: transmission check, STOP count, failure detection (FR1–FR4), unit status, repairs, acknowledgements, p, level bounds, and worst-case forecasts. The controller reads these through getters. `SteamBoilerProgram` drives the cycle and sends the failure-announcement and repair-acknowledgement messages. The trace covers all 53 requirements.

**Decisions the requirements left open:**
- Constants invented: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=2, P=10.
- Regulation: open all pumps below N1, close all above N2. Rescue regulates on the estimate's midpoint. Mode-changing cycles issue no pump orders. Initialization fills with pump 1 ("a pump").
- Level risk: the worst-case level one cycle ahead reaches M1 or M2. Against M2, sound pumps close instantly. Against M1, only pumps already pouring count (start-up latency).
- Emergency stop sends MODE once, then the program stops (SB-Beh22 over SB-Beh24).
- "Pump-controller units fail" means any pump controller. A level failure while the steam device or a pump controller is out of order goes straight to emergency stop (FR8).
- Aberrant messages:
  - a data message missing or repeated;
  - a pump number outside 1–4;
  - an acknowledgement with nothing to acknowledge;
  - STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization;
  - PHYSICAL_UNITS_READY before PROGRAM_READY.
- Separate enums for states and MODE values, so model names never clash.

**Unresolved:** failure detection and the failure/repair protocol sit outside the extracted state machine; the verifiers see only their results. Plausibility checks have no noise tolerance.
