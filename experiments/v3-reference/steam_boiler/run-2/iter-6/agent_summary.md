# Iteration 6 — steam_boiler, run 2

**Iteration 5 result.** FDR4 passed (3/3 assertions, 5 s) and vacuity passed cleanly, so cutting the free sensor facts to 4 fixed the state explosion. Only Isabelle failed: `deadlock_free` timed out again with 34 operations, although every state has an event-only operation. It has timed out at every size from 29 to 43; the corpus machines proved with that method have 3–5 operations. I conclude the proof cost is driven by the number of operations.

**Changes, to bring the controller down to 17 transitions (Java behaviour kept; simulations re-run):**
- The five emergency-stop findings become one event, `EmergencyStopDemanded`. The monitor still decides it per mode: STOP×3 and transmission failure in every mode, start-up failures in initialisation, M1/M2 risk outside it, steam or pump-controller failure in rescue.
- Regulation in normal, degraded and rescue (Beh10/15/19, per-cycle "state" behaviour) is applied by the transmission stage according to the controller's mode, with the same predictive rule.
- Initialisation's valve, pumps and PROGRAM_READY are controller decision fields. The transmission stage sends VALVE when the valve decision changes, the pump orders, and PROGRAM_READY every cycle until PHYSICAL_UNITS_READY.
- Initialisation now closes the valve and announces PROGRAM_READY in the same cycle.

**Cost.** The model keeps the mode logic and the initialisation handshake; which conditions demand emergency stop in which mode, and the regulation, are now in Java outside it.

**Decisions the requirements leave open:**
- invented constants: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10;
- pumps are assumed closed before any order;
- all four pumps switch together;
- initialisation fills with all pumps;
- in emergency stop the program only announces MODE;
- "aberrant" messages are enumerated;
- no level-dynamics check during initialisation;
- under FR8, a level failure goes to rescue first.
