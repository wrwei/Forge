package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Receives Move(lv, av) commands for the differential-drive layer and stores the last one (SR-DM4, SR-DM6).
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity;

    @RoboChartType("real")
    private double lastAngularVelocity;

    /**
     * Issues a Move command with the given linear (m/s) and angular (rad/s) velocity.
     */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLinearVelocity = lv;
        this.lastAngularVelocity = av;
    }

    /** Linear velocity of the last Move command. */
    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    /** Angular velocity of the last Move command. */
    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
