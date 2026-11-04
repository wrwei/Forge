package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives the controller's Move commands and stores the latest one (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Receives an output command from the controller.
     *
     * @param command the command issued
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move cmd = (OutputEvent.Move) command;
            this.lastLinearVelocity = cmd.lv();
            this.lastAngularVelocity = cmd.av();
        }
    }

    /** Returns the linear velocity of the last issued Move command (m/s). */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Returns the angular velocity of the last issued Move command (rad/s). */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
