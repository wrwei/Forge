package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * Reactive single-controller state machine for the SRanger ground
 * robot (SR-ARCH1, SR-ARCH2). Single-method, mode-nested if-else
 * structure; one outer branch per mode, named boolean predicates for
 * all guard conditions.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /** Time at which the controller most recently entered Turning (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        // SR-Beh1 / SR-FR1: initial mode is Moving; drive forward on entry.
        this.actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all transitions, event-triggered and autonomous.
     * Called once per control cycle.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - this.clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Mode-nested if-else: one outer branch per mode ---
        if (currentMode == SRangerMode.Moving) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving -> Halted on endTask; full stop on entry (SR-FR3).
                currentMode = SRangerMode.Halted;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving -> Turning on obstacle (guard SR-GP1);
                // record entry time and rotate in place (SR-FR2).
                currentMode = SRangerMode.Turning;
                this.clockResetTime = timer.now();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving -> Moving on tick, no action.
                currentMode = SRangerMode.Moving;
            }
        } else if (currentMode == SRangerMode.Turning) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning -> Halted on endTask; full stop on entry (SR-FR3).
                currentMode = SRangerMode.Halted;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Tick && turnDurationElapsed) {
                // SR-Beh5: Turning -> Moving when turn duration has elapsed
                // (guard SR-GP2); drive forward on entry (SR-FR1). Gated on
                // Tick (time is sampled each control cycle) so the Dafny
                // postcondition premise is event-specific and not violated
                // by the higher-priority EndTask branch.
                currentMode = SRangerMode.Moving;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh7: Turning -> Turning on tick, no action.
                currentMode = SRangerMode.Turning;
            }
        } else if (currentMode == SRangerMode.Halted) {
            if (event instanceof InputEvent.Tick) {
                // Halted is absorbing: tick self-loop, no action. Gives the
                // terminal mode a bare-precondition operation for the
                // Isabelle deadlock-freedom proof.
                currentMode = SRangerMode.Halted;
            }
        }
    }
}
