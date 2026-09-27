package sranger.controller;

import sranger.actuator.Actuator;
import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;
import sranger.event.InputEvent;
import sranger.event.OutputEvent;
import sranger.mode.SRangerMode;
import sranger.sensor.Sensor;
import sranger.time.Clock;

/**
 * Reactive SRanger state machine (SR-ARCH1, SR-ARCH2, SR-DC1).
 *
 * <p>Single-method, mode-nested if-else: the outer chain selects the current
 * mode, the inner chain selects the transition leaving it.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.MOVING;

    /** Time at which the controller most recently entered Turning, seconds (SR-Var1). */
    @RoboChartType("real")
    private double clockResetTime;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock cycleClock;

    public SRangerController(Sensor sensor, Actuator actuator, Clock cycleClock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.cycleClock = cycleClock;
        this.clockResetTime = cycleClock.nowSeconds();
        actuator.apply(new OutputEvent.Move(SRangerConstants.MOVE_VEL, 0.0));
    }

    /** The mode the controller is currently in. */
    public SRangerMode currentMode() {
        return currentMode;
    }

    /** Evaluates every transition leaving the current mode; called once per control cycle. */
    public void step(InputEvent event) {
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.OBSTACLE_THRESHOLD;
        boolean turnDurationElapsed =
                cycleClock.nowSeconds() - this.clockResetTime >= SRangerConstants.TURN_DURATION;

        if (currentMode == SRangerMode.MOVING) {
            if (event instanceof InputEvent.EndTask) {
                this.currentMode = SRangerMode.FINAL;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            } else if (event instanceof InputEvent.Obstacle && obstacleDetected) {
                this.currentMode = SRangerMode.TURNING;
                this.clockResetTime = cycleClock.nowSeconds();
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
            } else if (event instanceof InputEvent.Tick && !turnDurationElapsed) {
                this.currentMode = SRangerMode.TURNING;
            }

        } else if (currentMode == SRangerMode.FINAL) {
            // Terminal mode (SR-FR3): the requirements specify no transition
            // leaving Final.
        }
    }
}
