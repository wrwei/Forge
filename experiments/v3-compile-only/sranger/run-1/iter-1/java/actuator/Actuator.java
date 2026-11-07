package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the controller's Move commands and stores the latest one
 * for inspection (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity = 0.0;

    @RoboChartType("real")
    private double lastAngularVelocity = 0.0;

    /**
     * Applies a motor command issued by the controller.
     *
     * @param command the output event to apply
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move move = (OutputEvent.Move) command;
            this.lastLinearVelocity = move.lv();
            this.lastAngularVelocity = move.av();
        }
    }

    /**
     * Returns the linear velocity of the last issued Move command.
     *
     * @return the last commanded linear velocity (m/s)
     */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /**
     * Returns the angular velocity of the last issued Move command.
     *
     * @return the last commanded angular velocity (rad/s)
     */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
