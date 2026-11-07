package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;

/**
 * The SRanger controller (SR-ARCH1, SR-ARCH2): a reactive state machine with
 * the modes Moving (initial), Turning and Final. It reads the IR distance from
 * the {@link Sensor}, time from the {@link Clock}, and issues Move commands to
 * the {@link Actuator}.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** When the controller most recently entered Turning (s). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode, Moving, and performs the
     * entry action of Moving (forward Move command).
     *
     * @param sensor   the IR distance sensor
     * @param actuator the differential-drive command sink
     * @param timer    the time source
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /**
     * The current operating mode.
     *
     * @return the mode
     */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle: takes at most one transition of the current
     * mode, triggered by the given event or, for the autonomous Turning to
     * Moving transition, by elapsed time.
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
