package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives Move commands from the controller and stores the last one issued.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity;

    @RoboChartType("real")
    private double lastAngularVelocity;

    @RoboChartType("nat")
    private int commandCount;

    /**
     * Applies an output event to the differential-drive layer.
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLinearVelocity = move.lv();
            this.lastAngularVelocity = move.av();
            this.commandCount = this.commandCount + 1;
        }
    }

    /** Last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }

    /** Number of Move commands issued so far. */
    @RoboChartType("nat")
    public int commandCount() {
        return commandCount;
    }
}
