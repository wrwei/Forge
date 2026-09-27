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

    /** Applies an output event to the differential-drive layer. */
    public void apply(OutputEvent output) {
        if (output instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) output;
            this.lastLinearVelocity = move.lv();
            this.lastAngularVelocity = move.av();
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
}
