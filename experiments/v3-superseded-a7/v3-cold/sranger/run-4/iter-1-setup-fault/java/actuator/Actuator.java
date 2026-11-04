package sranger.actuator;

import sranger.annotation.RoboChartType;
import sranger.event.OutputEvent;

/**
 * Differential-drive actuator: receives Move commands and stores the last one issued.
 */
public final class Actuator {
    // Before any command has been issued the robot is at rest.
    @RoboChartType("real") private double lastLv = 0.0;
    @RoboChartType("real") private double lastAv = 0.0;

    /** Receives a Move command from the controller and records it. */
    public void apply(OutputEvent.Move cmd) {
        this.lastLv = cmd.lv();
        this.lastAv = cmd.av();
    }

    /** Returns the last commanded linear velocity, m/s. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLv;
    }

    /** Returns the last commanded angular velocity, rad/s. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAv;
    }
}
