package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Differential-drive actuator (SR-DM6).
 *
 * <p>{@link #move(double, double)} is the controller's single output: the
 * combined Move(lv, av) command of SR-DM4. The actuator retains the last
 * issued command for inspection.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVelocity = 0.0;

    @RoboChartType("real")
    private double lastAngularVelocity = 0.0;

    /** Issue Move(lv, av) to the differential-drive layer. */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLinearVelocity = lv;
        this.lastAngularVelocity = av;
    }

    @RoboChartType("real")
    public double lastLinearVelocity() {
        return lastLinearVelocity;
    }

    @RoboChartType("real")
    public double lastAngularVelocity() {
        return lastAngularVelocity;
    }
}
