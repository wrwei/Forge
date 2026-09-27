package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.clock.Clock;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.IrSensor;

/**
 * The SRanger reactive state machine (SR-ARCH1, SR-ARCH2, SR-DC1).
 *
 * <p>One public transition method, {@link #step(InputEvent)}, evaluates every
 * transition of every mode: the outer if-else chain selects the current mode,
 * the inner chain selects the transition by priority.
 */
public final class SRangerController {

    private final IrSensor irSensor;
    private final Actuator actuator;
    private final Clock cycleClock;

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** Time of the most recent entry into Turning, seconds (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(IrSensor irSensor, Actuator actuator, Clock cycleClock) {
        this.irSensor = irSensor;
        this.actuator = actuator;
        this.cycleClock = cycleClock;
        this.actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    public SRangerMode currentMode() {
        return currentMode;
    }

    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    public void step(InputEvent event) {
        // --- Named boolean predicates (SR-GP1, SR-GP2) ---
        boolean obstacleDetected = irSensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed =
                cycleClock.now() - this.clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Mode-nested if-else state machine ---
        if (currentMode == SRangerMode.MOVING) {
            // SR-Beh3: operator shutdown has priority in every mode.
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            // SR-Beh2
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                this.currentMode = SRangerMode.TURNING;
                this.clockResetTime = cycleClock.now();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            // SR-Beh4
            } else if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            // SR-Beh6: operator shutdown has priority in every mode.
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            // SR-Beh5: autonomous, guard-only — must outrank the tick self-loop,
            // otherwise the per-cycle tick would starve it.
            } else if (turnDurationElapsed) {
                this.currentMode = SRangerMode.MOVING;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            // SR-Beh7
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                this.currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // SR-FR3: terminal mode. The tick-triggered self-loop keeps Final
            // live (a visible event, not a tau-loop) so it is neither an
            // Isabelle deadlock nor an FDR4 divergence.
            if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.FINAL;
            }
        }
    }
}
