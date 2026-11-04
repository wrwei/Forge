package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle seen from the AUV: relative position north-south and east-west
 * (m), depth (m), velocity north-south and east-west (m/s) and rate of climb
 * (m/s).
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {

    /** True if both horizontal velocity components are zero. */
    public boolean isStatic() {
        return obs_ns_vel == 0.0 && obs_ew_vel == 0.0;
    }

    /** True if the obstacle is not static. */
    public boolean isDynamic() {
        return !isStatic();
    }
}
