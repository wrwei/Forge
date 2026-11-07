package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * Reactive single-controller state machine of the SRanger robot
 * (SR-ARCH1, SR-ARCH2). The robot drives forward in Moving, turns in place
 * for a fixed duration in Turning when an obstacle is detected, and stops in
 * the terminal mode Final when endTask arrives.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** Time at which the controller most recently entered Turning (seconds). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode, Moving, and issues the
     * Moving entry command Move(moveVel, 0).
     *
     * @param sensor   the IR distance sensor
     * @param actuator the differential-drive actuator
     * @param timer    the time source
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /**
     * Returns the current operating mode.
     *
     * @return the current mode
     */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle: handles the given input event and the
     * autonomous timed transition.
     *
     * @param event the input event delivered in this cycle
     */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.now();
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
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                currentMode = SRangerMode.TURNING;
            }
        } else if (currentMode == SRangerMode.FINAL) {
            // Terminal mode: no outgoing transitions.
        }
    }
}
