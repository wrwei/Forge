package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * The differential-drive layer: receives Move commands and stores the last
 * one issued.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinear = 0.0;
    @RoboChartType("real") private double lastAngular = 0.0;

    /**
     * Receives a motor command from the controller.
     *
     * @param command the output event to apply
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLinear = move.lv();
            this.lastAngular = move.av();
        }
    }

    /** Returns the linear velocity of the last command (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinear;
    }

    /** Returns the angular velocity of the last command (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngular;
    }
}
