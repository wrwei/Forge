package sranger.actuator;

import sranger.annotation.RoboChartType;

/**
 * Receives Move(lv, av) commands from the controller and stores the latest
 * issued linear and angular velocity for inspection (SR-DM4, SR-DM6).
 * Move is the only output the controller emits.
 */
public final class Actuator {

    @RoboChartType("real")
    private double lastLinearVel;

    @RoboChartType("real")
    private double lastAngularVel;

    /** Issue a combined linear / angular velocity command (the Move output event). */
    public void move(@RoboChartType("real") double lv, @RoboChartType("real") double av) {
        this.lastLinearVel = lv;
        this.lastAngularVel = av;
    }

    @RoboChartType("real")
    public double lastLinearVel() {
        return lastLinearVel;
    }

    @RoboChartType("real")
    public double lastAngularVel() {
        return lastAngularVel;
    }
}
