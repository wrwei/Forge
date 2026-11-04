package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;

/**
 * Reactive SRanger controller: drives forward, turns in place for a fixed
 * time when an obstacle is detected, and stops on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    // Time at which Turning was most recently entered (s).
    @RoboChartType("real") private double clockResetTime = 0.0;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode, Moving (SR-Beh1), and issues
     * the forward command of Moving's entry (SR-FR1).
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** The current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Handles one control cycle: the given input event and any autonomous transition. */
    public void step(InputEvent event) {
        // SR-GP1: the IR reading is at or below the obstacle-detection threshold.
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        // SR-GP2: the time since clockResetTime has reached the turn duration.
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving to Final; entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving to Turning; entry of Turning (SR-FR2, SR-Var1).
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving to Moving, no action.
                currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning to Final; entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: Turning to Moving (autonomous); entry of Moving (SR-FR1).
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: Turning to Turning, no action.
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // SR-FR3: terminal mode, no outgoing transitions.
        }
    }
}
