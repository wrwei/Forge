package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * The SRanger reactive controller (SR-ARCH1, SR-ARCH2): drives forward,
 * turns in place for a fixed time when an obstacle is detected, and stops
 * for good on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    @RoboChartType("real") private double clockResetTime = 0.0;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /**
     * Creates the controller in its initial mode, Moving, and performs
     * Moving's entry action: a forward Move command.
     *
     * @param sensor   the IR distance sensor
     * @param actuator the differential-drive actuator
     * @param clock    the time source
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** Returns the current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Executes one control cycle: takes at most one transition of the
     * current mode, chosen by the event received and the guard predicates.
     *
     * @param event the input event received in this cycle
     */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = clock.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2
                currentMode = SRangerMode.TURNING;
                clockResetTime = clock.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: no action
                currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: no action
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // Terminal mode (SR-FR3): no outgoing transitions.
        }
    }
}
