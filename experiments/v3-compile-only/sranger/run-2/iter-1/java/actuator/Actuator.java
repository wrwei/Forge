package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the controller's Move commands and stores the latest one
 * (SR-DM6). Before the first command the stored velocities are zero.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity = 0.0;

    @RoboChartType("real")
    private double lastAngularVelocity = 0.0;

    /**
     * Receives an output command and records it.
     *
     * @param command the command issued by the controller
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move issued = (OutputEvent.Move) command;
            this.lastLinearVelocity = issued.lv();
            this.lastAngularVelocity = issued.av();
        }
    }

    /**
     * Linear velocity of the last issued Move command.
     *
     * @return linear velocity in m/s
     */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /**
     * Angular velocity of the last issued Move command.
     *
     * @return angular velocity in rad/s
     */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
