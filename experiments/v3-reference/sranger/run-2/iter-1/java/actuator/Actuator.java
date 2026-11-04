package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives Move commands and keeps the last one issued.
 */
public final class Actuator {

    private OutputEvent lastCommand = new OutputEvent.Move(0.0, 0.0);
    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives a motor command from the controller and records it. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            lastCommand = move;
            lastLinearVelocity = move.lv();
            lastAngularVelocity = move.av();
        }
    }

    /** The last command issued. */
    public OutputEvent lastCommand() {
        return lastCommand;
    }

    /** The last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** The last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
