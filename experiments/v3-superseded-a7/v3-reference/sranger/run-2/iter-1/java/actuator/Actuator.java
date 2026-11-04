package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * The differential-drive layer's receiver of Move commands (SR-DM6). It stores
 * the last commanded linear and angular velocity for inspection.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives a command from the controller and records it. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLinearVelocity = move.lv();
            this.lastAngularVelocity = move.av();
        }
    }

    /** Linear velocity of the last command issued (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last command issued (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
