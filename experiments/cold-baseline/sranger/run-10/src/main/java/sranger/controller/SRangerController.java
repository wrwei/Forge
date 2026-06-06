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
 * Modes (SR-DM1): Moving (initial), Turning, Final.
 * Transitions (SR-Beh1..SR-Beh7) follow the requirement file.
 */
public final class SRangerController {

    private SRangerMode currentMode = SRangerMode.Moving;

    private final Sensor sensor;
    private final Actuator actuator;
    private final Clock clock;

    /** SR-Var1: timestamp recorded on entering Turning. */
    @RoboChartType("real")
    private double clockResetTime;

    public SRangerController(Sensor sensor, Actuator actuator, Clock clock) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.clock = clock;
        this.clockResetTime = 0.0;
        // SR-FR1: on power-up controller is in Moving; issue forward Move on entry.
        this.actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
    }

    public SRangerMode currentMode() {
        return this.currentMode;
    }

    @RoboChartType("real")
    public double clockResetTime() {
        return this.clockResetTime;
    }

    /**
     * Single-method, mode-nested if-else state machine.
     * SR-Beh1..SR-Beh7.
     */
    public void step(InputEvent event) {
        // --- Named boolean predicates (SR-GP1, SR-GP2) ---
        boolean obstacleDetected = sensor.distance() <= SRangerConstants.obstacleThreshold;
        boolean turnDurationElapsed = clock.now() - clockResetTime >= SRangerConstants.turnDuration;

        if (currentMode == SRangerMode.Moving) {
            // SR-Beh3: Moving -> Final on endTask (highest priority)
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh2: Moving -> Turning on obstacle
            else if (event instanceof InputEvent.Obstacle) {
                currentMode = SRangerMode.Turning;
                clockResetTime = clock.now();
                actuator.apply(new OutputEvent.Move(0.0, SRangerConstants.turnVel));
            }
            // SR-Beh4: Moving -> Moving on tick (no action)
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Moving;
            }

        } else if (currentMode == SRangerMode.Turning) {
            // SR-Beh6: Turning -> Final on endTask (highest priority)
            if (event instanceof InputEvent.EndTask) {
                currentMode = SRangerMode.Final;
                actuator.apply(new OutputEvent.Move(0.0, 0.0));
            }
            // SR-Beh5: Turning -> Moving when turn duration elapsed (autonomous)
            else if (turnDurationElapsed) {
                currentMode = SRangerMode.Moving;
                actuator.apply(new OutputEvent.Move(SRangerConstants.moveVel, 0.0));
            }
            // SR-Beh7: Turning -> Turning on tick (no action)
            else if (event instanceof InputEvent.Tick) {
                currentMode = SRangerMode.Turning;
            }

        } else if (currentMode == SRangerMode.Final) {
            // Terminal mode — no outgoing transitions; remain in Final.
        }
    }
}
