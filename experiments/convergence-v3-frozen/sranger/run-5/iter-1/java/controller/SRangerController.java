package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.IrSensor;
import sranger.time.Clock;

/**
 * The SRanger reactive state machine (SR-ARCH1, SR-ARCH2, SR-DC1).
 *
 * <p>Single-method, mode-nested if-else: the outer chain selects the current
 * mode, the inner chain selects the transition leaving it.
 */
public final class SRangerController {

    private final IrSensor irSensor;
    private final Actuator actuator;
    private final Clock cycleClock;

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** Time at which the controller most recently entered Turning (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime = 0.0;

    public SRangerController(IrSensor irSensor, Actuator actuator, Clock cycleClock) {
        this.irSensor = irSensor;
        this.actuator = actuator;
        this.cycleClock = cycleClock;
        this.actuator.move(SRangerConstants.MOVE_VEL, 0.0);
    }

    /** The mode the controller is currently in (SR-DM1). */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Timestamp of the most recent Turning entry (SR-Var1). */
    @RoboChartType("real")
    public double clockResetTime() {
        return clockResetTime;
    }

    public void step(InputEvent event) {
        boolean obstacleDetected = irSensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed =
                cycleClock.nowSeconds() - this.clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                this.currentMode = SRangerMode.TURNING;
                this.clockResetTime = cycleClock.nowSeconds();
                actuator.move(0.0, SRangerConstants.TURN_VEL);
            } else if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.MOVING;
            }

        } else if (currentMode == SRangerMode.TURNING) {
            if (turnDurationElapsed) {
                this.currentMode = SRangerMode.MOVING;
                actuator.move(SRangerConstants.MOVE_VEL, 0.0);
            } else if (event instanceof InputEvent.EndTask && !turnDurationElapsed) {
                this.currentMode = SRangerMode.FINAL;
                actuator.move(0.0, 0.0);
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                this.currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            if (event instanceof InputEvent.Tick) {
                this.currentMode = SRangerMode.FINAL;
            }
        }
    }
}
