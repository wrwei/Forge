package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * SRanger reactive controller: a single-method, mode-nested if-else state
 * machine over the modes Moving, Turning and Stopped (terminal).
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /** Time (seconds) at which the controller most recently entered Turning. */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        // Power-up entry action of the initial Moving mode: drive forward.
        actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Evaluates all event-triggered and autonomous transitions; called once per control cycle. */
    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Pure mode-nested if-else: one outer block per mode ---
        if (currentMode == SRangerMode.Moving) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Stopped;
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
                currentMode = SRangerMode.Stopped;
                actuator.move(0.0, 0.0);
            }
            // Autonomous (guard-only) transition: turn duration elapsed.
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Stopped) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Stopped;
            }
        }
    }
}
