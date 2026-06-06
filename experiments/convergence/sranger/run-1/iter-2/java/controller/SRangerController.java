package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;

/**
 * Reactive single-controller state machine for the SRanger ground robot
 * (SR-ARCH1, SR-ARCH2). Three modes — Moving (initial), Turning, Final —
 * selected by a mode-nested if-else over the input event, the IR distance,
 * and the elapsed time since entering Turning.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock cycleClock = new Clock();

    /** Time (seconds) at which the controller most recently entered Turning (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        // Initial entry action of the initial mode Moving (SR-FR1, SR-Beh1).
        this.actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed =
                cycleClock.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.Moving) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                currentMode = SRangerMode.Turning;
                this.clockResetTime = cycleClock.now();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }
        } else if (currentMode == SRangerMode.Turning) {
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Tick && turnDurationElapsed) {
                // Timed Turning -> Moving (SR-Beh5, SR-GP2). Gated on the tick
                // event so the transition's premise is event-specific: this keeps
                // the higher-priority endTask branch from making the (otherwise
                // autonomous) postcondition unprovable when both fire on one step.
                currentMode = SRangerMode.Moving;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }
        } else if (currentMode == SRangerMode.Final) {
            // Terminal mode: the robot stays halted. A bare tick self-loop keeps
            // Final deadlock-free (every state needs one enabled, bare-precondition
            // operation) without ever leaving the mode; no Move is re-issued.
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Final;
            }
        }
    }
}
