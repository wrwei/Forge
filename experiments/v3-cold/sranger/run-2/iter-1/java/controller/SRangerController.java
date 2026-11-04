package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * Reactive SRanger controller (SR-ARCH1, SR-ARCH2): drives forward in Moving,
 * turns in place for the configured duration after an obstacle, and stops in
 * Final when the operator issues endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    @RoboChartType("real") private double clockResetTime = 0.0;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode Moving and issues the entry
     * action of Moving.
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
     * Runs one control cycle: takes at most one transition of the current mode,
     * triggered by {@code event} or, for the timed Turning to Moving transition,
     * by the elapsed time alone.
     *
     * @param event the input event of this cycle, or {@code null} if none
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
