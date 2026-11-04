package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * The SRanger reactive controller (SR-ARCH1, SR-ARCH2): drives forward in
 * Moving, turns in place for a fixed duration in Turning after an obstacle,
 * and stops in the terminal mode Final on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    // Time at which the controller most recently entered Turning (SR-Var1).
    @RoboChartType("real") private double clockResetTime;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Powers the controller up in Moving and issues Moving's entry command
     * Move(moveVel, 0) (SR-Beh1, SR-FR1).
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** The current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Processes one input event; called once per control cycle. */
    public void step(InputEvent event) {
        // --- Guard predicates ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- One block per mode; transitions in priority order ---
        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving -> Final, entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving -> Turning, entry of Turning (SR-FR2, SR-Var1).
                currentMode = SRangerMode.TURNING;
                clockResetTime = timer.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving -> Moving, no action.
                currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning -> Final, entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning -> Moving, entry of Moving (SR-FR1).
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: Turning -> Turning, no action.
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // Terminal mode (SR-FR3): no outgoing transitions.
        }
    }
}
