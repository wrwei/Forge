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
 * SRanger reactive single-controller state machine (SR-ARCH1, SR-ARCH2).
 *
 * <p>Implements the three operating modes Moving (initial), Turning, and
 * Final, with seven labelled transitions SR-Beh1..SR-Beh7.</p>
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /**
     * Timestamp (milliseconds) when the controller most recently entered
     * the Turning state (SR-Var1). The guard {@code turnDurationElapsed}
     * uses this together with {@code clock.nowMs()} to fire the autonomous
     * Turning → Moving transition.
     */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // Entry action of the initial Moving state (SR-FR1, SR-Beh1).
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() { return currentMode; }

    @RoboChartType("real")
    public double clockResetTime() { return clockResetTime; }

    /**
     * Single-step transition selector. Evaluates all transitions
     * (event-triggered and autonomous) for the current mode.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (SR-GP1, SR-GP2) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed = clock.nowMs() - clockResetTime >= SRangerConstants.turnDurationMs;

        if (currentMode == SRangerMode.Moving) {
            // Highest priority: operator shutdown (SR-Beh3).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // Obstacle-triggered transition to Turning (SR-Beh2).
            else if (event instanceof InputEvent.Obstacle) {
                clockResetTime = clock.nowMs();
                currentMode = SRangerMode.Turning;
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // Tick self-loop (SR-Beh4). No action performed.
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // Highest priority: operator shutdown (SR-Beh6).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // Autonomous Turning → Moving when the turn duration has elapsed (SR-Beh5).
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // Tick self-loop (SR-Beh7). No action performed.
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Final is terminal (SR-FR3). No outgoing transitions.
        }
    }
}
