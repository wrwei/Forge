package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive command sink; stores the last issued Move command.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    private OutputEvent lastCommand;

    /** Receives an output event from the controller. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLv = move.lv();
            this.lastAv = move.av();
            this.lastCommand = move;
        }
    }

    /** Last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLv;
    }

    /** Last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAv;
    }

    /** Last issued command, or null if none has been issued. */
    public OutputEvent lastCommand() {
        return lastCommand;
    }
}
