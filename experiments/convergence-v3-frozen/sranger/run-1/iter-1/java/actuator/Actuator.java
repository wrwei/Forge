package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * The differential-drive output layer (SR-DM6). It receives the controller's
 * only output, a combined linear / angular velocity command, and stores the
 * last issued values for inspection.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity = 0.0;

    @RoboChartType("real")
    private double lastAngularVelocity = 0.0;

    /**
     * Issues a Move command to the differential-drive layer (SR-DM4).
     *
     * @param lv target linear velocity, m/s
     * @param av target angular velocity, rad/s
     */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLinearVelocity = lv;
        this.lastAngularVelocity = av;
    }

    /** Linear velocity of the last issued Move command. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last issued Move command. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
