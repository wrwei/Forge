# Steam Boiler — iteration 2

**Iteration 1 failures and changes:**
- Preflight: a `double` constant lacked `@RoboChartType("real")`. Every double field and helper is now annotated.
- FDR4 was killed at 80 GB, and Isabelle `deadlock_free` timed out. Guards called real-valued sensor functions (level estimate, forecasts, steam rate) compared against constants. Every sensor query is now boolean; `BoilerSensor.judge` makes the comparisons. Behaviour is unchanged. The STEAM_BOILER_WAITING latch and the mode-dependent aberrant-message checks moved into the sensor. The valve state is a boolean field.
- Vacuity crashed: `RecursionError` in the audit's conjunction parser, a likely pipeline defect. Workaround: every guard is a conjunction of boolean judgements or their negations, with no `||`, no negated compound and no `!=`. Within each mode, branches are exclusive and cover every case.
- `EMERGENCY_STOP` now loops on each cycle, sending MODE(EMERGENCY_STOP) only (SB-Beh24). There is still no way out and no control action (SB-Beh22).

**Design (unchanged):** one `TransmissionReceived` trigger per cycle after `BoilerSensor.receive` takes the whole transmission (SB-V3). The sensor does failure detection, unit status, the estimate and forecasts. `SteamBoilerProgram` sends failure announcements and repair acknowledgements.

**Decisions the requirements left open:**
- Constants invented: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=2, P=10.
- Regulation: open all pumps below N1, close all above N2. Rescue regulates on the estimate's midpoint. Initialization fills with pump 1.
- Level risk: the worst-case level one cycle ahead reaches M1 or M2, assuming the best response and the pump start-up latency.
- "Pump-controller units fail" means any pump controller. A level failure while steam or a pump controller is out of order goes straight to emergency stop.
- Aberrant: a data message missing or repeated, a bad pump number, an unexpected acknowledgement, start-up messages outside initialization, or PHYSICAL_UNITS_READY before PROGRAM_READY.

**Unresolved:** the detection internals are outside the verified model.
