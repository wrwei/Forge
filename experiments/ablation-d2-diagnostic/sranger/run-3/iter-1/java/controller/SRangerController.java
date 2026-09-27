package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * Reactive SRanger controller: drives forward, turns in place on an obstacle, stops on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    @RoboChartType("real")
    private double clockResetTime;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in Moving and issues the Moving entry command.
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.clockResetTime = timer.nowMs();
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** Current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Time (ms) at which Turning was most recently entered. */
    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    /**
     * Executes one control cycle for the given input event.
     */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.nowMs() - clockResetTime >= SRangerConstants.TURN_DURATION_MS;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.nowMs();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.MOVING;
            }
        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
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
