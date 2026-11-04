package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle known to the AUV (LRE-DM2). The relative distances give the
 * obstacle's position minus the AUV's position (metres); the velocities are the
 * obstacle's own (m/s). The obstacle is static when both horizontal velocities
 * are zero, and dynamic otherwise.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
