package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/** Receives Move commands from the controller and stores the last issued one (SR-DM6). */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity;

    @RoboChartType("real")
    private double lastAngularVelocity;

    /** Applies an output event issued by the controller. */
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
