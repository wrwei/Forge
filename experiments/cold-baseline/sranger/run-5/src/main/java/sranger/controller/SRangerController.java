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
 * SRanger reactive single-controller state machine.
 *
 * Implements the mode-nested if-else state machine pattern required for
 * RoboChart extraction. Three modes (Moving, Turning, Final), seven
 * transitions defined by SR-Beh1 through SR-Beh7.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /** Timestamp (seconds) recorded on entry to Turning; used by the turn-duration guard. */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // Initial mode entry action: SR-FR1 / SR-Beh1 — issue Move(moveVel, 0) on start.
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed = clock.nowMs() - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else state machine ---
        if (currentMode == SRangerMode.Moving) {
            // SR-Beh3: Moving -> Final on endTask (highest priority operator override)
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh2: Moving -> Turning on obstacle (with obstacleDetected guard)
            else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.nowMs();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // SR-Beh4: Moving -> Moving on tick (no action)
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // SR-Beh6: Turning -> Final on endTask (highest priority operator override)
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh5: Turning -> Moving (autonomous) when turnDurationElapsed
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // SR-Beh7: Turning -> Turning on tick (no action)
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Terminal state — Final remains Final on any further event.
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Final;
            }
        }
    }
}
