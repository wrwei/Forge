package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.timing.Clock;

/**
 * Reactive SRanger state machine: drive forward, turn in place on an obstacle, stop on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    @RoboChartType("real")
    private double clockResetTime;

    /**
     * Creates the controller in its initial mode, Moving, and issues the forward Move command.
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    /** Current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates one control step for the given input event.
     */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.nowMs() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.nowMs();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.MOVING;
            }
        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            } else if (turnDurationElapsed) {
                currentMode = SRangerMode.MOVING;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.TURNING;
            }
        } else if (currentMode == SRangerMode.FINAL) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.FINAL;
            }
        }
    }
}
