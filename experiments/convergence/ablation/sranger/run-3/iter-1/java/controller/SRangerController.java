package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Clock;
import sranger.sensor.Sensor;

/**
 * Reactive single-controller state machine for the SRanger ground robot.
 * Single-method, mode-nested if-else state machine over three modes:
 * Moving (initial), Turning, and Stopped (terminal, entered on EndTask).
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;
    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /** Time (seconds) at which the controller most recently entered Turning. */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
    }

    /** Current operating mode. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Evaluates all transitions for one control step. */
    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == SRangerMode.Moving) {
            // High-priority shutdown first (SR-Beh3)
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Stopped;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: obstacle -> Turning; entry: clock reset + Move(0, turnVel)
                currentMode = SRangerMode.Turning;
                this.clockResetTime = timer.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: tick self-loop, no action
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // High-priority shutdown duplicated in this mode block (SR-Beh6)
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Stopped;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning -> Moving; entry: Move(moveVel, 0)
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh7: tick self-loop, no action
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Stopped) {
            // Terminal mode stays live: tick self-loop gives the state a
            // bare-precondition operation (no RoboChart Final state).
            if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Stopped;
            }
        }
    }
}
