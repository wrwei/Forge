package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives Move commands from the controller and stores the latest
 * issued command for inspection.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    /** Applies a motor command, recording its velocities. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLv = move.lv();
            this.lastAv = move.av();
        }
    }

    /** Last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    /** Last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }
}
