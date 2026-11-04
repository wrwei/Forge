package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives the controller's Move commands and
 * stores the last one issued.
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
            OutputEvent.Move moveCommand = (OutputEvent.Move) command;
            this.lastLinearVelocity = moveCommand.lv();
            this.lastAngularVelocity = moveCommand.av();
        }
    }

    /** @return linear velocity of the last Move command (m/s) */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** @return angular velocity of the last Move command (rad/s) */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
