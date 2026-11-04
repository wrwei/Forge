package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * The SRanger reactive controller: drives forward, turns in place for a fixed
 * time when an obstacle is reported, and stops for good on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    // When the controller most recently entered Turning (s).
    @RoboChartType("real") private double clockResetTime = 0.0;

    /**
     * Creates the controller in Moving and issues Moving's forward command.
     *
     * @param sensor   the IR distance sensor
     * @param actuator the differential-drive layer
     * @param timer    the time source for the timed Turning to Moving transition
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
     * Runs one control cycle: takes at most one transition from the current
     * mode, triggered by the given event or, for the timed Turning to Moving
     * transition, by elapsed time alone.
     *
     * @param event the input event delivered this cycle
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
