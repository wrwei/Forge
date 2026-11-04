# Steam Boiler — iteration 3

**Iteration 2:** preflight and vacuity passed. FDR4 was killed at 80 GB again, and Isabelle `deadlock_free` timed out.

**Changes, Java behaviour unchanged (same outputs on my scenario runs):**
- Isabelle needs, in every mode, one branch with no guard besides the event. The last branch of each mode is now `event instanceof TransmissionReceived`. That is the remaining case of the else-if chain: the idle wait in initialization, otherwise the ready announcement or the hold.
- FDR4: numeric types were already narrowed to {0..1}, so value ranges were not the cause. Guards repeated the negations of every higher branch (up to ten sensor calls each, roughly 40 to 90 per mode). Guards now carry at most two boolean judgements, and the else-if order supplies the priority. Negations are only required below predicate-only branches, which I don't have. Trade-off: the extracted model lets a mode's transitions overlap; it over-approximates the Java's priority order.

**Earlier changes:** boolean sensor judgements (the comparisons are made in `BoilerSensor.judge`). `EMERGENCY_STOP` reports MODE every cycle and has no exit. The vacuity audit's crash in iteration 1 (`RecursionError` in its guard parser) looks like a pipeline defect; flat guards avoid it.

**Design:** one `TransmissionReceived` per cycle after `BoilerSensor.receive`. The sensor does detection, unit status, the estimate and forecasts. `SteamBoilerProgram` sends failure announcements and repair acknowledgements.

**Open decisions:**
- Constants invented: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=2, P=10.
- Regulation is bang-bang on all pumps; initialization fills with pump 1.
- Level risk is the worst case one cycle ahead, with the pump start-up latency.
- Any pump-controller failure makes rescue untenable.
- Aberrant: a missing or repeated data message, a bad pump number, an unexpected acknowledgement, a start-up message outside initialization, or PHYSICAL_UNITS_READY before PROGRAM_READY.

**Unresolved:** detection internals lie outside the model.
