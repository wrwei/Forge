package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Receives Move commands from the controller and stores the last one issued
 * (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Records a motor command issued by the controller.
     *
     * @param command the output event to apply
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move issued = (OutputEvent.Move) command;
            this.lastLinearVelocity = issued.lv();
            this.lastAngularVelocity = issued.av();
        }
    }

    /** @return the linear velocity of the last Move command (m/s) */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** @return the angular velocity of the last Move command (rad/s) */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
