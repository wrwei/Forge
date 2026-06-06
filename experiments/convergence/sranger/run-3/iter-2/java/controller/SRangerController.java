package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;

/**
 * SRanger reactive single-controller state machine (SR-ARCH1, SR-ARCH2).
 *
 * <p>Three modes (SR-DM1): Moving (initial), Turning, Stopped. The robot
 * drives forward in Moving, rotates in place in Turning for a fixed
 * duration, and stops permanently in Stopped (the terminal mode) when the
 * operator issues endTask. The Move command issued on entering each mode is
 * the mode's entry action (SR-FR1 … SR-FR3).</p>
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /** Time the controller most recently entered Turning, seconds (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: evaluate all event-triggered and autonomous
     * transitions for the current mode (SR-Beh1 … SR-Beh7).
     */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed =
                clock.now() - clockResetTime >= SRangerConstants.turnDuration;

        if (currentMode == SRangerMode.Moving) {
            actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Stopped;
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                clockResetTime = clock.now();
                currentMode = SRangerMode.Turning;
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Stopped;
            } else if (event instanceof InputEvent.Tick && turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
            } else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Stopped) {
            actuator.apply(new OutputEvent.Move(0.0, 0.0));
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Stopped;
            }
        }
    }
}
