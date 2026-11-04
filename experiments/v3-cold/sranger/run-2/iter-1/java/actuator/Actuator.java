package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives Move commands from the controller and stores the last one issued
 * (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Receives a motor command and records it as the latest issued command.
     *
     * @param command the command issued by the controller
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move moveCommand = (OutputEvent.Move) command;
            this.lastLinearVelocity = moveCommand.lv();
            this.lastAngularVelocity = moveCommand.av();
        }
    }

    /**
     * Returns the linear velocity of the last issued command.
     *
     * @return the last commanded linear velocity, m/s
     */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /**
     * Returns the angular velocity of the last issued command.
     *
     * @return the last commanded angular velocity, rad/s
     */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
