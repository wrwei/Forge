package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive output boundary (SR-DM6). Stores the last issued command.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity;

    @RoboChartType("real")
    private double lastAngularVelocity;

    /** Receives a Move command from the controller and records it. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLinearVelocity = move.lv();
            this.lastAngularVelocity = move.av();
        }
    }

    /** Last commanded linear velocity, m/s. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Last commanded angular velocity, rad/s. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
