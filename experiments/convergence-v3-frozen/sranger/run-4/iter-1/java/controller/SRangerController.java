package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;

/**
 * Reactive single-controller state machine for SRanger (SR-ARCH1, SR-ARCH2, SR-DC1).
 *
 * <p>Single-method, mode-nested if-else: the outer chain selects the current mode and the
 * inner chain selects the transition leaving that mode, highest priority first.</p>
 */
public final class SRangerController {

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock timer;

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** Time at which the controller most recently entered Turning, milliseconds (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(Sensor sensor, Actuator actuator, Clock timer) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.timer = timer;
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** The mode the controller is currently in. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Time of the most recent entry into Turning, milliseconds. */
    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    /** Evaluates every transition leaving the current mode for one control cycle. */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed =
                timer.nowMs() - clockResetTime >= SRangerConstants.TURN_DURATION_MS;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                this.currentMode = SRangerMode.TURNING;
                this.clockResetTime = timer.nowMs();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.TURN_VEL));
            } else if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (turnDurationElapsed) {
                this.currentMode = SRangerMode.MOVING;
                actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
            } else if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.FINAL;
            }
        }
    }
}
