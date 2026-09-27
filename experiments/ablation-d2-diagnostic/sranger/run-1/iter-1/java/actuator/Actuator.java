package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/** Receives Move commands and stores the last one issued. */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    private boolean commanded;

    /** Accepts an output event from the controller. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLv = move.lv();
            this.lastAv = move.av();
            this.commanded = true;
        }
    }

    /** Last commanded linear velocity. */
    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    /** Last commanded angular velocity. */
    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }

    /** Whether any command has been issued yet. */
    public boolean hasCommand() {
        return commanded;
    }
}
