package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Receives motor commands from the controller and stores the last-issued
 * command for inspection. The single command channel carries a combined
 * linear / angular velocity target for the differential-drive layer.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    /** Issues a Move command with target linear and angular velocity. */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLv = lv;
        this.lastAv = av;
    }

    /** Last commanded linear velocity (m/s). */
    @RoboChartType("real")
    public double lastLv() {
        return lastLv;
    }

    /** Last commanded angular velocity (rad/s). */
    @RoboChartType("real")
    public double lastAv() {
        return lastAv;
    }
}
