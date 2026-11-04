package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle as seen from the AUV (LRE-DM2). Component names follow the
 * requirement; distances in metres, velocities in m/s.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {

    /** True when both horizontal velocity components are zero; otherwise the obstacle is dynamic. */
    public boolean isStatic() {
        return obs_ns_vel == 0.0 && obs_ew_vel == 0.0;
    }
}
