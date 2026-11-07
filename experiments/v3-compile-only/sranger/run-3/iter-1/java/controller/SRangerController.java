package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;

/**
 * The SRanger reactive controller: a single state machine over the modes
 * Moving, Turning and Final.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    @RoboChartType("real") private double clockResetTime = 0.0;

    /**
     * Creates the controller in its initial mode, Moving (SR-Beh1), and
     * performs Moving's entry action: a forward Move command (SR-FR1).
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

    /** Executes one control cycle for the given input event. */
    public void step(InputEvent event) {
        // Guard predicates (SR-GP1, SR-GP2).
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
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
                // SR-Beh4: Moving to Moving on tick, no action.
                currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning to Final; entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning to Moving; entry of Moving (SR-FR1).
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: Turning to Turning on tick, no action.
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // Terminal mode (SR-FR3): no outgoing transitions.
        }
    }
}
