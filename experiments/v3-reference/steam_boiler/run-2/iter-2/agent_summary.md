# Iteration 2 — steam_boiler, run 2

**Iteration 1 result.** Four phases failed: preflight (an unannotated `double` parameter), FDR4 (killed at 82 GB), Isabelle (`deadlock_free` timed out) and vacuity (parser recursion). The last three point to the guards: deeply inlined predicates over six real-valued sensor functions.

**Changes (behaviour kept):**
- Every quantity in `BoilerSensor` is now one of 13 boolean facts. `UnitMonitor` makes the comparisons with N1, N2, M1 and M2, including the M1/M2 risk given this cycle's pump decision.
- Every controller guard is a flat conjunction of literals: no composite predicates, no `||`, no parentheses. Each emergency-stop cause has its own branch, giving 40 transitions. A brute-force check over all assignments confirms that each mode's guards are exclusive and exhaustive.
- Beh3 (steam ≠ 0 at start-up) is now a steam-device failure judged by the monitor, as the requirement words it.
- Initialisation records STEAM_BOILER_WAITING and starts its checks on the next cycle. After draining it closes the valve one cycle before filling or announcing. An abort no longer closes the valve.
- Monitor bug found by simulation: a repaired pump's controller is now given the two-transmission settling time (previously a false pump-controller failure).
- `@RoboChartType` added to the remaining `double`/`int` parameters and returns.

**Decisions the requirements leave open:**
- invented constants: C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10;
- pumps are assumed closed before any order;
- all four pumps switch together, with predictive regulation;
- initialisation fills with all pumps;
- emergency stop is terminal (sends MODE once);
- "aberrant" messages are enumerated;
- no level-dynamics check during initialisation (valve rate unknown);
- under FR8, a level failure goes to rescue before a steam or pump-controller emergency.

**Unresolved.** The monitor's logic is outside the model, so the verifiers do not check it.
