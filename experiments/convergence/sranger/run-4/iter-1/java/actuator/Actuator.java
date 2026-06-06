package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Differential-drive actuator (SR-DM6). Receives Move(lv, av) commands
 * from the controller and stores the latest issued command for
 * inspection (SR-DM4).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    /** Issues a combined linear / angular velocity command. */
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
