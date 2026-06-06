package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Receives Move(lv, av) commands from the controller and stores the
 * last issued command for inspection (SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLv;

    @RoboChartType("real")
    private double lastAv;

    /**
     * Issues a combined linear / angular velocity command to the
     * differential-drive layer (SR-DM4).
     */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLv = lv;
        this.lastAv = av;
    }

    /** Last commanded linear velocity (m/s). */
    public double lastLv() {
        return lastLv;
    }

    /** Last commanded angular velocity (rad/s). */
    public double lastAv() {
        return lastAv;
    }
}
