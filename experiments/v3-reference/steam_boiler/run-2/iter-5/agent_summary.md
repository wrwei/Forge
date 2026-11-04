# Iteration 5 — steam_boiler, run 2

**Iteration 4 result.** FDR4 and Isabelle failed again; everything else passed.
- FDR4 kept discovering data-parameterised process instances (230,000 and growing) until it was killed.
- Isabelle's `deadlock_free` still timed out although every state now had an event-only operation, so that diagnosis was not the cause.

Both point to model size: 43 operations over 8 free boolean facts plus 4 state booleans.

**Changes (Java behaviour kept; simulations re-run, including new start-up-steam and rescue-failure scenarios):**
- Two monitor findings that only ever lead to emergency stop are now events, like LevelRisk:
  - `StartUpFailure`: in initialisation after STEAM_BOILER_WAITING, the level device failed or the steam rate is not zero (Beh3, Beh6). It now stops in the arrival cycle itself.
  - `EstimateUnreliable`: in rescue, the steam device or a pump controller failed (Beh19, Beh21).
- The four level comparisons become two facts, `levelTooLow` and `levelTooHigh`. They compare the reading with N1/N2 in initialisation and the prediction elsewhere.
- Sensor facts drop from 8 to 4.
- Branches that only stayed and did nothing are removed: in Java they are identical to no branch matching. Operations drop from 43 to 34. EMERGENCY_STOP keeps its event-only self-loop.

**Decisions the requirements leave open:**
- invented constants: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10;
- pumps are assumed closed before any order;
- all four pumps switch together, with predictive regulation;
- initialisation fills with all pumps;
- in emergency stop the program only announces MODE;
- "aberrant" messages are enumerated;
- no level-dynamics check during initialisation;
- under FR8, a level failure goes to rescue first.

**Unresolved.** Detection, bookkeeping and the protocol are outside the extracted model.
