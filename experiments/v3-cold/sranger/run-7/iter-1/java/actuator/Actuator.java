package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the controller's Move commands and records the last one issued.
 */
public final class Actuator {

    // Stationary until the first command arrives.
    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Receives an output command from the controller.
     *
     * @param command the issued command
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            lastLinearVelocity = move.lv();
            lastAngularVelocity = move.av();
        }
    }

    /** @return the last commanded linear velocity (m/s) */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** @return the last commanded angular velocity (rad/s) */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
