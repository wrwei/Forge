# Iteration 4 — steam_boiler, run 2

**Iteration 3 result.** Everything passed except FDR4 (killed at 82 GB again) and Isabelle (`deadlock_free` timed out again). By iteration 3, every non-terminal mode had an event-only transition, but EMERGENCY_STOP had no operation at all, and the directive says the tactic needs one in *every* state.

**Changes:**
- EMERGENCY_STOP has an event-only self-loop with no action. This resolves the SB-Beh22/SB-Beh24 ambiguity in favour of SB-Beh24: in emergency stop the program analyses and controls nothing, but keeps announcing MODE(emergency_stop) every cycle.
- The controller now decides and the transmission stage encodes, to shrink the model's actions.
  - MODE(m) is sent by the program on every cycle from the controller's mode, instead of by each of the 43 transitions.
  - The pump orders are one controller decision field, `pumpsOpen`. The monitor turns it into OPEN/CLOSE_PUMP(n) for each pump whose last order differs; this also re-orders a just-repaired pump.
  - The controller's own outputs are now only VALVE and PROGRAM_READY.
- Mode behaviour is unchanged: the simulations reproduce the same mode sequences, and redundant repeated pump orders are gone.

**Decisions the requirements leave open:**
- invented constants: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10;
- pumps are assumed closed before any order;
- all four pumps switch together, with predictive regulation;
- initialisation fills with all pumps, starts its checks the cycle after STEAM_BOILER_WAITING, and closes the valve before filling or announcing;
- "aberrant" messages are enumerated;
- no level-dynamics check during initialisation;
- under FR8, a level failure goes to rescue first.

**Unresolved.** Detection, bookkeeping and the failure/repair protocol stay outside the extracted model.
