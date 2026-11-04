# Steam Boiler — iteration 4

**Iteration 3:** FDR4 was killed at 80 GB again. Isabelle hit its wall-clock limit (exit 142, about 618 s) in iterations 2 and 3, which had 41 operations each. Iteration 1 had 30 operations and reached the `deadlock_free` tactic in time.

**Changes (Java behaviour unchanged):**
- `BoilerSensor.judge` now combines the conditions that end in emergency stop:
  - `initializationAbortDemanded`: three STOPs, transmission failure, or, after STEAM_BOILER_WAITING, nonzero steam or a failed level device;
  - `emergencyStopDemanded`: three STOPs, transmission failure, worst-case level risk, or the level unknowable (level device lost while steam or a pump controller is out of order).
- The controller reads 8 boolean judgements (was 11) and has 29 transitions (was 41).
- An exhaustive check (18,432 cases) shows the old and new decision logic agree; scenario runs give identical output.
- Kept: a bare-event fallback in every mode (the Isabelle tactic needs it), and guards of at most two judgements, with else-if order giving priority. The model therefore over-approximates that order.

**Earlier:** the vacuity audit crashed in iteration 1 (`RecursionError` in its guard parser), a likely pipeline defect; flat guards avoid it. `EMERGENCY_STOP` reports MODE every cycle and has no exit.

**Design:** one `TransmissionReceived` per cycle after `BoilerSensor.receive`. The sensor does detection, unit status, the estimate and forecasts. `SteamBoilerProgram` sends failure announcements and repair acknowledgements.

**Open decisions:**
- Constants invented: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=2, P=10.
- Regulation is bang-bang on all pumps; initialization fills with pump 1.
- Level risk is the worst case one cycle ahead, with pump start-up latency.
- Any pump-controller failure makes rescue untenable.
- Aberrant: a missing or repeated data message, a bad pump number, an unexpected acknowledgement, a start-up message outside initialization, or PHYSICAL_UNITS_READY before PROGRAM_READY.

**Unresolved:** detection internals lie outside the model.
