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
 * SRanger reactive state-machine controller.
 *
 * Modes (SR-DM1): Moving (initial), Turning, Final.
 *
 * Transitions implemented (SR-Beh1 .. SR-Beh7):
 *   - SR-Beh1: initial mode = Moving (field initialiser).
 *   - SR-Beh2: Moving -- Obstacle --> Turning
 *              (entry: clockResetTime := clock.nowMs(); Move(0, turnVel)).
 *   - SR-Beh3: Moving -- EndTask  --> Final   (entry: Move(0, 0)).
 *   - SR-Beh4: Moving -- Tick     --> Moving  (no action).
 *   - SR-Beh5: Turning -- [turnDurationElapsed] --> Moving (entry: Move(moveVel, 0)).
 *   - SR-Beh6: Turning -- EndTask --> Final   (entry: Move(0, 0)).
 *   - SR-Beh7: Turning -- Tick    --> Turning (no action).
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /** Time (ms) of the most recent Moving -> Turning entry (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Single-method state-machine step. Evaluates one input event under the
     * current mode and updates mode/outputs accordingly.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (SR-GP1, SR-GP2) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed = clock.nowMs() - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else (outer: currentMode == X) ---
        if (currentMode == SRangerMode.Moving) {
            // Highest priority: operator shutdown (SR-Beh3).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle) {
                // SR-Beh2: Moving -> Turning on obstacle (entry per SR-FR2).
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.nowMs();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving -> Moving on tick, no action.
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // Highest priority: operator shutdown (SR-Beh6).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning -> Moving when timed out (entry per SR-FR1).
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh7: Turning -> Turning on tick, no action.
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Terminal mode: no outgoing transitions per SR-FR3.
            // No event handling needed; controller remains in Final.
        }
    }
}
