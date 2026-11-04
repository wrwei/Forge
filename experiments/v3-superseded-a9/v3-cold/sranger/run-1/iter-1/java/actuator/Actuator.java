package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives Move commands from the controller and keeps the last one issued.
 */
public final class Actuator {

    private OutputEvent.Move lastCommand = new OutputEvent.Move(0.0, 0.0);

    /**
     * Issues a command to the differential-drive layer and records it.
     *
     * @param command the command issued by the controller
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastCommand = move;
        }
    }

    /** The last Move command issued (Move(0, 0) before any command). */
    public OutputEvent.Move lastCommand() {
        return lastCommand;
    }

    /** The last commanded linear velocity, m/s. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastCommand.lv();
    }

    /** The last commanded angular velocity, rad/s. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastCommand.av();
    }
}
