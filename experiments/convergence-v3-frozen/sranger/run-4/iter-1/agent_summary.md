# sranger run 4 — iteration 1 (cold codegen)

## What I wrote

`java.generated.project/src/main/java/` was empty — nothing to delete. I created
nine files under `sranger/`: `annotation/RoboChartType`, `mode/SRangerMode`
(MOVING/TURNING/FINAL), `event/InputEvent` (Obstacle, Tick, EndTask signals) and
`event/OutputEvent` (Move(lv, av)), `constants/SRangerConstants`,
`sensor/Sensor` (distance(), large default 1000.0), `actuator/Actuator`,
`controller/Clock` (nowMs()), and `controller/SRangerController`. Plus
`result_codegen.json` — 53 entries, all 23 requirements and all 9 files covered.
`javac --release 17` is clean.

The controller is a single-method, mode-nested if-else with two named
predicates (`obstacleDetected`, `turnDurationElapsed`) declared before the
chain. Priority in each mode: endTask first, then the mode's own transitions.

## Decisions the requirements did not settle

1. **Clock unit.** CLAUDE.md recognises clock promotion only from
   `clock.nowMs()`, i.e. milliseconds, but SR-DM2 states turnDuration in
   seconds. I kept `TURN_DURATION = 2.0` (the spec constant) and added a
   derived `TURN_DURATION_MS = 2000.0`, which is what the guard compares
   against. The integer value also survives the CSP-gen constant ceiling.
2. **Moving → Turning trigger vs guard.** SR-Beh2 says the transition fires on
   the `obstacle` event; SR-GP1 says `obstacleDetected` guards the same
   transition. I conjoined both (`instanceof Obstacle && obstacleDetected`),
   which satisfies both texts literally. If the framework can emit `obstacle`
   without a matching `distance()` update, this is stricter than SR-Beh2 alone.
3. **FINAL liveness.** `java_codegen_rules.txt` requires every mode to have an
   outgoing transition and mandates a *tick-triggered* self-loop on the
   terminal mode; CLAUDE.md instead advises removing `Final` entirely from the
   theory-generated controller. Those conflict, and dropping FINAL would
   contradict SR-DM1/SR-FR3/SR-Beh3/SR-Beh6, which are immutable input. I
   followed the rules file: FINAL keeps a `Tick` self-loop. Flagging for human
   review — if Isabelle `deadlock_free` fails here, the conflict is real.
4. **Initial entry action.** SR-FR1's Move(moveVel, 0) on entering Moving is
   issued from the constructor, since Moving is the power-up mode (SR-Beh1).

Nothing unresolved.
