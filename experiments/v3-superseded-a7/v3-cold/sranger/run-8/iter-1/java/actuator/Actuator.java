package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * The differential-drive actuator (SR-DM6). Receives Move commands from the
 * controller and stores the last one issued.
 */
public final class Actuator {

    // Stopped until the first command arrives.
    private OutputEvent.Move lastCommand = new OutputEvent.Move(0.0, 0.0);

    /**
     * Receives an output command from the controller.
     *
     * @param command the command issued
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move cmd = (OutputEvent.Move) command;
            this.lastCommand = cmd;
        }
    }

    /** Returns the last Move command issued. */
    public OutputEvent.Move lastCommand() {
        return lastCommand;
    }

    /** Returns the last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastCommand.lv();
    }

    /** Returns the last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastCommand.av();
    }
}
