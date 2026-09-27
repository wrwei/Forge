package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * The SRanger reactive state machine (SR-ARCH1, SR-ARCH2). It drives forward,
 * turns in place when the IR sensor reports an obstacle, returns to driving
 * once the configured turn duration has elapsed, and stops on endTask.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock cycleClock;

    /** Time at which the controller most recently entered Turning, in seconds (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock cycleClock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.cycleClock = cycleClock;
        actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    /** The mode the controller is currently in. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Time at which the controller most recently entered Turning, in seconds. */
    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    /**
     * Evaluates every transition leaving the current mode, event-triggered and
     * autonomous alike. Called once per control cycle.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed =
                cycleClock.nowSeconds() - this.clockResetTime >= SRangerConstants.TURN_DURATION;

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == SRangerMode.MOVING) {
            // SR-Beh3: shutdown has the highest priority in this mode.
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            }
            // SR-Beh2 / SR-GP1: obstacle ahead, start turning in place.
            else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                this.currentMode = SRangerMode.TURNING;
                this.clockResetTime = cycleClock.nowSeconds();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            }
            // SR-Beh4: keep driving.
            else if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            // SR-Beh6: shutdown has the highest priority in this mode.
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            }
            // SR-Beh5 / SR-GP2: autonomous, and ahead of the tick self-loop so the
            // per-cycle tick cannot starve it.
            else if (turnDurationElapsed) {
                this.currentMode = SRangerMode.MOVING;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            }
            // SR-Beh7: keep turning.
            else if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // Terminal mode: an idle tick self-loop keeps it live for the
            // extracted model (a mode with no outgoing transition is a deadlock).
            if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.FINAL;
            }
        }
    }
}
