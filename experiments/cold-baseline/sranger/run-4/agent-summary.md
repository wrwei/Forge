# SRanger cold-baseline run 4 — agent summary

Single-shot codegen, no prior verifier feedback, no prior session.

## Files written

Under `src/main/java/sranger/`:

- `annotation/RoboChartType.java`, `annotation/Clock.java`, `annotation/SensorService.java`
- `mode/SRangerMode.java` — three-mode enum (Moving initial, Turning, Final).
- `constants/SRangerConstants.java` — moveVel=1.0, turnVel=2.0, obstacleThreshold=0.5,
  turnDuration=2.0 (s) plus a derived `turnDurationMs=2000.0` so the
  M2M's `clock.nowMs() - <field> >= CONST` → `since(...)` rewrite applies cleanly.
- `event/InputEvent.java` (sealed, three signal records: Obstacle, Tick, EndTask).
- `event/OutputEvent.java` (sealed, single Move(lv, av) record).
- `sensor/Sensor.java` — distance() with a large default for un-initialised readings.
- `actuator/Actuator.java` — stores lastLv / lastAv from Move.
- `clock/Clock.java` — `nowMs()` for the time-predicate rewrite.
- `controller/SRangerController.java` — pure two-level mode-nested if-else,
  two named predicates (`obstacleDetected`, `turnDurationElapsed`).
- `Main.java` — minimal demo entry point.

## Design choices

- Tick events kept as event-triggered self-loops in Moving and Turning
  (SR-Beh4, SR-Beh7) — gives bare-precondition coverage for those states
  without introducing a τ self-loop that breaks FDR4 determinism.
- Final has no outgoing transitions per SR-FR3; this is a strict reading
  of the terminal-state requirement.
- EndTask has highest priority in both non-Final modes (duplicated first
  in each block).
