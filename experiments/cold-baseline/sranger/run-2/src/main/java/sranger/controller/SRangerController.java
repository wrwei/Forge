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
 * SRanger reactive single-controller state machine with three operating
 * modes: Moving (initial), Turning, Final.
 *
 * Implements requirements SR-FR1..SR-FR3, SR-GP1..SR-GP2, SR-Var1,
 * SR-Beh1..SR-Beh7, SR-DC1.
 *
 * Mode-nested single-method if-else state machine: outer chain selects the
 * current mode, inner chains select transitions ordered by priority.
 * Guard conditions are captured as named boolean predicates before the
 * outer chain.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /** SR-Var1: time (seconds) when controller most recently entered Turning. */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // SR-Beh1 / SR-FR1: initial mode is Moving; issue Move(moveVel, 0)
        // as the initial entry action on power-up.
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /**
     * Single control-step entry point. Evaluates all transitions
     * (event-triggered and autonomous) for the current mode.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        // SR-GP1: obstacleDetected — IR distance at or below threshold.
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        // SR-GP2: turnDurationElapsed — elapsed time since clockResetTime
        // at or above turnDuration.
        boolean turnDurationElapsed = clock.nowSec() - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else state machine ---
        if (currentMode == SRangerMode.Moving) {
            // SR-Beh3: Moving -> Final on endTask (highest priority).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle) {
                // SR-Beh2: Moving -> Turning on obstacle. Entry action of
                // Turning: record clockResetTime; issue Move(0, turnVel).
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.nowSec();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving -> Moving on tick (self-loop, no action).
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // SR-Beh6: Turning -> Final on endTask (highest priority).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning -> Moving when
                // turnDurationElapsed. Entry action of Moving:
                // issue Move(moveVel, 0).
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh7: Turning -> Turning on tick (self-loop, no action).
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // SR-FR3: Final is terminal — no outgoing transitions.
            // Self-loop to keep deadlock-freedom well-defined.
            currentMode = SRangerMode.Final;
        }
    }
}
