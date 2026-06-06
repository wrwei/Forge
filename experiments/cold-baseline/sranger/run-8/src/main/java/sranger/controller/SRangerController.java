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
 * SRanger controller: a single-method, mode-nested if-else state machine
 * with three modes (Moving, Turning, Final).
 *
 * The outer if-else chain selects the current mode (one block per mode,
 * using {@code currentMode == X} only). The inner if-else chain within
 * each mode block selects transitions ordered by priority. High-priority
 * transitions (endTask -> Final) are duplicated as the first inner branch
 * in each non-Final mode block.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /**
     * Time (seconds) at which the controller most recently entered the
     * Turning state. Set as an entry action on Moving -> Turning.
     */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // Entry action of the initial state (Moving): Move(moveVel, 0).
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    /**
     * Single step of the state machine. Handles both event-triggered
     * transitions (obstacle, tick, endTask) and the autonomous
     * Turning -> Moving transition.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed =
                clock.nowSeconds() - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else ---
        if (currentMode == SRangerMode.Moving) {
            // High-priority endTask -> Final
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle) {
                // Moving -> Turning: record clock reset time, enter Turning.
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.nowSeconds();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            } else if (event instanceof InputEvent.Tick) {
                // Moving -> Moving (tick self-loop, no action). Bare-precondition
                // cover for deadlock-freedom.
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // High-priority endTask -> Final
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // Autonomous Turning -> Moving.
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            } else if (event instanceof InputEvent.Tick) {
                // Turning -> Turning (tick self-loop, no action). Bare-precondition
                // cover for deadlock-freedom.
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Terminal mode — no outgoing transitions.
        }
    }
}
