package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives {@link OutputEvent.Move} commands from the controller and stores the
 * latest issued linear / angular velocity for inspection (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    /** Receive a motor command and store it as the last-issued command. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLv = move.lv();
            this.lastAv = move.av();
        }
    }

    /** The last commanded linear velocity. */
    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    /** The last commanded angular velocity. */
    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }
}
