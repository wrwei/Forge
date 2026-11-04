# Steam boiler, pass 9, iteration 1

## What I wrote

There are 11 files under `steam_boiler/`. No Java was present beforehand.

- **`SteamBoilerController`** holds the mode (initialization first) and two handshake flags. Its `step` reacts to `InputEvent.TransmissionComplete`, which ends each cycle's reception. The guards within each mode exclude one another and together cover every case. Priority runs emergency stop, then rescue, then degraded, then regulation (SB-FR8). Emergency stop is terminal.
- **`BoilerSensor`** collects each cycle's messages. It judges unit and transmission failures (SB-FR1–5) and maintains q, v, p, the level bounds, the unit status and the STOP count (SB-V5–10).
- **`BoilerActuator`** builds the outgoing transmission. Pump and valve orders are sent only on change. Failure announcements and repair acknowledgements are emitted on every cycle (SB-FR6/7).

Why: per-unit history cannot be written as simple predicates in a two-level if-else. Keeping it in the leaves keeps the controller's model small. Every transition is triggered by an event, which avoids divergence.

## Decisions the requirements did not settle

- **Constants:** C=1000, M1=100, M2=900, N1=400, N2=600, W=25, U1=U2=2, P=10, cycle 5 s, STOP threshold 3.
- **Regulation:** all pumps on below N1; all off when the worst-case next-cycle level exceeds N2.
- **Level risk:** low limit assumes maximal steam and no pumping; high limit assumes every pump that is on, flowing or uncertain pours.
- **Initialization:** checks begin after STEAM_BOILER_WAITING. Pump 1 fills. Level dynamics are not checked while the valve is open (no evacuation rate given).
- **Rescue:** any pump-controller failure means emergency stop.
- **After emergency stop:** no MODE message.
- **Aberrant messages:** my own list, in `post_codegen.md`.

## Not resolved

- The constants are placeholders.
- In rescue mode the widening estimate makes the pumps alternate on and off.
