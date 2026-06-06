package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.clock.Clock;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;

/**
 * SRanger reactive state machine controller (SR-ARCH2, SR-FR1..3, SR-Beh1..7).
 *
 * Single-method, mode-nested if-else state machine. The outer chain
 * dispatches on currentMode; the inner chains dispatch on event/guard
 * with priority order:
 *   1. endTask (operator shutdown — highest priority)
 *   2. mode-specific event/guard transitions
 *   3. tick self-loop
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // SR-Beh1 / SR-FR1: initial mode Moving issues Move(moveVel, 0) on entry.
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        // Named boolean predicates (declared BEFORE the if-else chain).
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed =
                clock.nowMs() - clockResetTime >= SRangerConstants.turnDuration;

        // Pure mode-nested if-else: one outer branch per mode.
        if (currentMode == SRangerMode.Moving) {
            // SR-Beh3: endTask -> Final (highest priority).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh2: obstacle -> Turning.
            else if (event instanceof InputEvent.Obstacle) {
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.nowMs();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // SR-Beh4: tick -> Moving (self-loop, no action).
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // SR-Beh6: endTask -> Final (highest priority, duplicated in this mode).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh5: autonomous turnDurationElapsed -> Moving.
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // SR-Beh7: tick -> Turning (self-loop, no action).
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Terminal mode (SR-FR3) — no outgoing transitions.
            // Provide a tick self-loop with no action so the state has bare-precondition cover.
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Final;
            }
        }
    }
}
