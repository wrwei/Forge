package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * Reactive single state-machine controller of the SRanger robot.
 * Drives forward in Moving, rotates in place in Turning, and halts in
 * the absorbing Halted mode when the operator issues endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /** Time at which the controller most recently entered Turning. */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.clockResetTime = 0.0;
        actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.nowMs() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.Moving) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Halted;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.Turning;
                clockResetTime = timer.nowMs();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }
        } else if (currentMode == SRangerMode.Turning) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Halted;
                actuator.move(0.0, 0.0);
            } else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }
        } else if (currentMode == SRangerMode.Halted) {
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Halted;
            }
        }
    }
}
