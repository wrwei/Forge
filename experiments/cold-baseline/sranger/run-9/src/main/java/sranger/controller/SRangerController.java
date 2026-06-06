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
 * SRanger reactive single-controller state machine.
 *
 * Modes: Moving (initial), Turning, Final.
 * Transitions:
 *   SR-Beh2 Moving -> Turning on Obstacle  (entry: record clock, Move(0, turnVel))
 *   SR-Beh3 Moving -> Final   on EndTask   (entry: Move(0, 0))
 *   SR-Beh4 Moving -> Moving  on Tick      (no action)
 *   SR-Beh5 Turning -> Moving autonomous when turnDurationElapsed (entry: Move(moveVel, 0))
 *   SR-Beh6 Turning -> Final  on EndTask   (entry: Move(0, 0))
 *   SR-Beh7 Turning -> Turning on Tick     (no action)
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        // Initial-state entry action (SR-FR1, SR-Beh1): Move(moveVel, 0)
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed = clock.nowSeconds() - clockResetTime >= SRangerConstants.turnDuration;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == SRangerMode.Moving) {
            // SR-Beh3: highest priority — operator shutdown
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh2: obstacle event triggers Moving -> Turning
            else if (event instanceof InputEvent.Obstacle) {
                clockResetTime = clock.nowSeconds();
                currentMode = SRangerMode.Turning;
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // SR-Beh4: tick — self-loop, no action
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // SR-Beh6: highest priority — operator shutdown
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh5: autonomous Turning -> Moving when turn duration elapsed
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // SR-Beh7: tick — self-loop, no action
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Final is terminal — no outgoing transitions per requirements.
            // Self-loop on tick to ensure bare-precondition coverage for
            // the Isabelle deadlock-freedom proof.
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Final;
            }
        }
    }
}
