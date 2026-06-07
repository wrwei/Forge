package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Receives Move(lv, av) motor commands from the controller and stores
 * the latest issued command for inspection.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVel;

    @RoboChartType("real")
    private double lastAngularVel;

    /** Issues a combined linear / angular velocity command to the differential-drive layer. */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLinearVel = lv;
        this.lastAngularVel = av;
    }

    /** Last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLinearVel() {
        return lastLinearVel;
    }

    /** Last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAngularVel() {
        return lastAngularVel;
    }
}
