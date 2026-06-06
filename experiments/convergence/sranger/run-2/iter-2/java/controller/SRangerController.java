package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * The SRanger reactive controller (SR-ARCH1, SR-ARCH2): a single-method,
 * mode-nested if-else state machine over the three modes Moving, Turning and
 * Final.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluate all transitions (event-triggered and autonomous) for the current
     * control cycle.
     */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed = timer.nowMs() - clockResetTime >= SRangerConstants.turnDuration;

        if (currentMode == SRangerMode.Moving) {
            actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Stopped;
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                clockResetTime = timer.nowMs();
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
