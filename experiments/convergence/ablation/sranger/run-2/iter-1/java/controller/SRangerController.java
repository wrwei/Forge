package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * SRanger reactive controller: a single-method, mode-nested if-else
 * state machine over the modes Moving, Turning, and Halted.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /** Time (seconds) when the controller most recently entered Turning. */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        // Power-up: the controller begins in Moving and drives forward.
        this.actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all event-triggered and autonomous transitions for the
     * current control cycle.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == SRangerMode.Moving) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Halted;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.Turning;
                clockResetTime = timer.now();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Halted;
                actuator.move(0.0, 0.0);
            }
            // Autonomous (guard-only) transition — no event check
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Halted) {
            // Absorbing terminal mode: remains Halted on every control cycle.
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Halted;
            }
        }
    }
}
