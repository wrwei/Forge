package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.clock.Clock;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;

/**
 * SRanger reactive controller.
 *
 * SR-ARCH1, SR-ARCH2, SR-DM1, SR-Beh1..SR-Beh7.
 *
 * Single-method, mode-nested if-else state machine. Modes:
 * Moving (initial), Turning, Final.
 */
public final class SRangerController {

    private SRangerMode mode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // SR-FR1: on power-up the controller is in Moving — issue Move(moveVel, 0).
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return mode;
    }

    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    public void step(InputEvent event) {
        // --- Named boolean predicates (SR-GP1, SR-GP2) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed =
                ((double) clock.nowMs() / 1000.0) - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else state machine ---
        if (mode == SRangerMode.Moving) {
            // SR-Beh3: Moving -> Final on endTask (highest priority among Moving outgoings)
            if (event instanceof InputEvent.endTask) {
                mode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh2: Moving -> Turning on obstacle
            else if (event instanceof InputEvent.obstacle) {
                mode = SRangerMode.Turning;
                clockResetTime = (double) clock.nowMs() / 1000.0;
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // SR-Beh4: Moving -> Moving on tick (no action)
            else if (event instanceof InputEvent.tick) {
                mode = SRangerMode.Moving;
            }
        } else if (mode == SRangerMode.Turning) {
            // SR-Beh6: Turning -> Final on endTask (highest priority)
            if (event instanceof InputEvent.endTask) {
                mode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh5: Turning -> Moving when turnDurationElapsed (autonomous, no event)
            else if (turnDurationElapsed) {
                mode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // SR-Beh7: Turning -> Turning on tick (no action)
            else if (event instanceof InputEvent.tick) {
                mode = SRangerMode.Turning;
            }
        } else if (mode == SRangerMode.Final) {
            // Terminal mode — no outgoing transitions.
        }
    }
}
