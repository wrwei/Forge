package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;

/**
 * Reactive SRanger controller: drives forward, turns in place for a fixed
 * time when an obstacle is detected, and stops on endTask
 * (SR-ARCH1, SR-ARCH2, SR-DC1).
 */
public final class SRangerController {

    // SR-Beh1: the controller starts in Moving.
    private SRangerMode currentMode = SRangerMode.MOVING;

    // SR-Var1: time at which Turning was most recently entered.
    @RoboChartType("real") private double clockResetTime = 0.0;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in Moving and issues Moving's entry command
     * Move(moveVel, 0) (SR-FR1, SR-Beh1).
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

    /** Returns the current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle for the given input event.
     *
     * @param event the input event of this cycle
     */
    public void step(InputEvent event) {
        // SR-GP1: the IR distance is at or below the obstacle threshold.
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        // SR-GP2: the time since clockResetTime is at or above the turn duration.
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving -> Final; entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving -> Turning; entry of Turning (SR-FR2).
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving -> Moving on tick, no action.
                currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning -> Final; entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning -> Moving; entry of Moving (SR-FR1).
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: Turning -> Turning on tick, no action.
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // SR-FR3: Final is terminal; it has no outgoing transitions.
        }
    }
}
