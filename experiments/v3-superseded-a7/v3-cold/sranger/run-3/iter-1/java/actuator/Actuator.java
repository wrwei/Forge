package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * The differential-drive layer: receives the controller's Move commands and
 * keeps the last one issued for inspection. Before any command it reports a
 * stopped robot.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /** Receives a motor command from the controller. */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move issued = (OutputEvent.Move) command;
            this.lastLinearVelocity = issued.lv();
            this.lastAngularVelocity = issued.av();
        }
    }

    /** Linear velocity of the last command issued, m/s. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last command issued, rad/s. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
