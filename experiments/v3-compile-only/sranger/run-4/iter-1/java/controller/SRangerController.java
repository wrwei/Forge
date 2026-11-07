package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * Reactive SRanger controller (SR-ARCH1, SR-ARCH2): drives forward in Moving,
 * turns in place for turnDuration seconds after an obstacle, and stops in
 * Final on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** Time at which the controller most recently entered Turning (SR-Var1). */
    @RoboChartType("real") private double clockResetTime = 0.0;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /**
     * Creates the controller in its initial mode, Moving, and issues Moving's
     * entry command Move(moveVel, 0) (SR-Beh1, SR-FR1).
     *
     * @param sensor   the IR distance sensor
     * @param actuator the differential-drive actuator
     * @param timer    the controller's time source
     */
    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /**
     * @return the current operating mode
     */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Executes one control cycle: takes at most one transition of the current
     * mode, chosen by the input event, the IR distance and the time in Turning.
     *
     * @param event the input event delivered this cycle
     */
    public void step(InputEvent event) {
        // Guard predicates (SR-GP1, SR-GP2).
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving -> Final; entry of Final (SR-FR3).
                currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving -> Turning; entry of Turning (SR-FR2, SR-Var1).
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
                // SR-Beh5: Turning -> Moving, autonomous; entry of Moving (SR-FR1).
                currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                // SR-Beh7: Turning -> Turning on tick, no action.
                currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // SR-FR3: terminal mode, no outgoing transitions.
        }
    }
}
