package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the controller's Move commands and stores the latest one (SR-DM6).
 * Before any command is issued the stored velocities are zero.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives an output command from the controller. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move moveCommand = (OutputEvent.Move) command;
            this.lastLinearVelocity = moveCommand.lv();
            this.lastAngularVelocity = moveCommand.av();
        }
    }

    /** Linear velocity of the last issued command (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last issued command (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
