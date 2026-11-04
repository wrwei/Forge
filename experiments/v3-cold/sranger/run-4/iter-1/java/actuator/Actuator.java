package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives Move commands and stores the last one issued.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Receives an output command from the controller and records it.
     *
     * @param command the command issued
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLinearVelocity = move.lv();
            this.lastAngularVelocity = move.av();
        }
    }

    /**
     * Returns the last commanded linear velocity.
     *
     * @return linear velocity in m/s
     */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /**
     * Returns the last commanded angular velocity.
     *
     * @return angular velocity in rad/s
     */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
