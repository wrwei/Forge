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
 * SRanger reactive state-machine controller.
 *
 * Mode-nested single-method state machine implementing SR-Beh1..SR-Beh7:
 *   - Moving  --Obstacle--> Turning      (record clockResetTime, Move(0,turnVel))
 *   - Moving  --EndTask -->  Final        (Move(0, 0))
 *   - Moving  --Tick    -->  Moving       (no-op)
 *   - Turning --turnDurationElapsed--> Moving  (autonomous; Move(moveVel,0))
 *   - Turning --EndTask -->  Final        (Move(0, 0))
 *   - Turning --Tick    -->  Turning      (no-op)
 *
 * Initial mode is Moving (SR-Beh1); on construction the entry action
 * Move(moveVel, 0) is issued.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /** SR-Var1: time at which the controller last entered Turning. */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // SR-FR1 / SR-Beh1: entry action of the initial Moving state.
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    /**
     * Single step of the controller. Evaluates ALL transitions
     * (event-triggered and autonomous) for the current mode.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        // SR-GP1: IR distance at or below the obstacle-detection threshold.
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        // SR-GP2: time since clockResetTime reached the turn duration.
        boolean turnDurationElapsed = clock.now() - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == SRangerMode.Moving) {
            // SR-Beh3: EndTask wins (terminal override).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh2: obstacle event triggers turn.
            else if (event instanceof InputEvent.Obstacle) {
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // SR-Beh4: Moving -> Moving on Tick, no action.
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // SR-Beh6: EndTask wins (terminal override).
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh5: autonomous Turning -> Moving when the turn duration elapses.
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // SR-Beh7: Turning -> Turning on Tick, no action.
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Terminal mode: no outgoing transitions.
        }
    }
}
