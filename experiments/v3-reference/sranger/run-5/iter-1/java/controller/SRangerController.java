package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;
import sranger.timing.Clock;

/**
 * Reactive SRanger controller: drives forward, turns in place for a fixed
 * time when an obstacle is reported, and stops for good on endTask
 * (SR-ARCH1, SR-ARCH2).
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    @RoboChartType("real") private double clockResetTime = 0.0;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode, Moving, and issues the
     * Moving entry command Move(moveVel, 0).
     *
     * @param sensor   IR distance sensor
     * @param actuator differential-drive actuator
     * @param timer    time source
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** @return the current operating mode */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle for the given input event.
     *
     * @param event the input event delivered in this cycle
     */
    public void step(InputEvent event) {
        // --- Named guard predicates ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- One block per mode; transitions in priority order ---
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
                // Autonomous: no event required.
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
