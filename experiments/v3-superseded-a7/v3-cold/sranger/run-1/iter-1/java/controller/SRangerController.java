package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;

/**
 * Reactive SRanger controller: drives forward, turns in place on an
 * obstacle, and stops on endTask (SR-ARCH1, SR-ARCH2).
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    @RoboChartType("real") private double clockResetTime = 0.0;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode, Moving, and issues Moving's
     * entry command (SR-Beh1, SR-FR1).
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** The controller's current mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Handles one control cycle: event-triggered and autonomous transitions. */
    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Two-level if-else: one block per mode ---
        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving to Final
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving to Turning
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving to Moving, no action
                currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning to Final
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: Turning to Moving (autonomous)
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: Turning to Turning, no action
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // SR-FR3: terminal mode, no outgoing transitions
        }
    }
}
