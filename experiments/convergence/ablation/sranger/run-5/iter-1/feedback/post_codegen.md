# Post-codegen review — sranger, iter 1 (cold codegen)

**Status:** complete — all 22 requirements implemented (8 Java files + `result_codegen.json`).

## Design choices for review

1. **Terminal mode named `Stopped`, not `Final`** (SR-DM1, SR-FR3, SR-Beh3, SR-Beh6).
   CLAUDE.md mandates the theory-generated controller must not contain a `Final`
   state (the Isabelle `deadlock_free` closer hangs on one). The terminal mode is
   `Stopped`: behaviourally terminal (its only outgoing transition is a `Tick`
   self-loop — which also provides the preferred event-triggered bare-precondition
   cover), entry action `move(0, 0)` preserved.

2. **`Move(lv, av)` encoded as the multi-arg `Actuator.move(lv, av)` call**
   (SR-DM4, SR-DM6). RoboChart events carry one payload; the ETL's designed
   mapping for two-payload commands is an `LOperations` operation call. No
   `OutputEvent` sealed interface exists.

3. **Moving→Turning conjoins trigger and guard** (SR-Beh2 + SR-GP1):
   `event instanceof InputEvent.Obstacle && obstacleDetected`.

4. **Invented default:** Sensor no-reading default = `100.0` m (SR-DM5 says only
   "a large default value").

5. **Power-up Moving entry action** (`move(MOVE_VEL, 0)`) fired once in the
   controller constructor; constructors are not model-extracted, the model's
   initial transition covers it there (SR-FR1, SR-Beh1).

**Next step:** run the deterministic pipeline and act on visible feedback.
