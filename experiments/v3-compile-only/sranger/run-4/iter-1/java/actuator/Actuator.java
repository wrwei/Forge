package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator (SR-DM6): receives Move commands from the
 * controller and stores the last one issued for inspection.
 */
public final class Actuator {

    @RoboChartType("real") private double lastLinearVelocity = 0.0;
    @RoboChartType("real") private double lastAngularVelocity = 0.0;

    /**
     * Receives an output event from the controller and records it.
     *
     * @param command the command issued by the controller
     */
    public void apply(OutputEvent command) {
        if (command instanceof OutputEvent.Move) {
            OutputEvent.Move mv = (OutputEvent.Move) command;
            this.lastLinearVelocity = mv.lv();
            this.lastAngularVelocity = mv.av();
        }
    }

    /**
     * @return the last commanded linear velocity (m/s)
     */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /**
     * @return the last commanded angular velocity (rad/s)
     */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
