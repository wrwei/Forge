package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives Move commands and records the last
 * one issued.
 */
public final class Actuator {

    // Before any command the robot is stationary.
    @RoboChartType("real")
    private double lastLinearVelocity = 0.0;

    @RoboChartType("real")
    private double lastAngularVelocity = 0.0;

    /** Receives a command from the controller and records it. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move moveCommand = (OutputEvent.Move) command;
            this.lastLinearVelocity = moveCommand.lv();
            this.lastAngularVelocity = moveCommand.av();
        }
    }

    /** Returns the linear velocity of the last Move command (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Returns the angular velocity of the last Move command (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
