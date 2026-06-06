package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.IrSensor;

/**
 * SRanger reactive controller (SR-ARCH1, SR-ARCH2): a single-method,
 * mode-nested if-else state machine over the modes of {@link SRangerMode}.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;
    private final IrSensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    /** Time (seconds) at which the controller most recently entered Turning (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(IrSensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        // SR-Beh1 / SR-FR1: power-up enters Moving; issue the forward command.
        actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Evaluates all transitions (event-triggered and autonomous) once per control cycle. */
    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed = timer.now() - clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Pure mode-nested if-else: one outer block per mode ---
        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh3: Moving -> Halted on endTask; stop (SR-FR3).
                currentMode = SRangerMode.HALTED;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                // SR-Beh2: Moving -> Turning on obstacle (guard SR-GP1);
                // record the entry time and turn in place (SR-FR2).
                currentMode = SRangerMode.TURNING;
                this.clockResetTime = timer.now();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh4: Moving -> Moving on tick; no action.
                currentMode = SRangerMode.MOVING;
            }
        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                // SR-Beh6: Turning -> Halted on endTask; stop (SR-FR3).
                currentMode = SRangerMode.HALTED;
                actuator.move(0.0, 0.0);
            } else if (turnDurationElapsed) {
                // SR-Beh5: autonomous Turning -> Moving when the turn
                // duration has elapsed (guard SR-GP2); drive forward (SR-FR1).
                currentMode = SRangerMode.MOVING;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.Tick) {
                // SR-Beh7: Turning -> Turning on tick; no action.
                currentMode = SRangerMode.TURNING;
            }
        } else if (currentMode == SRangerMode.HALTED) {
            if (event instanceof InputEvent.Tick) {
                // Halted is absorbing: tick self-loop keeps the terminal
                // mode live for deadlock-freedom (bare-precondition cover).
                currentMode = SRangerMode.HALTED;
            }
        }
    }
}
