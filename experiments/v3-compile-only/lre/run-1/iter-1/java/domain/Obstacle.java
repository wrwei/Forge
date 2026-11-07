package lre.domain;

import lre.annotation.RoboChartType;

/**
 * An obstacle as seen from the AUV (LRE-DM2). Relative distances and depth in
 * metres, velocities in m/s. An obstacle is static when both horizontal
 * velocity components are zero, dynamic otherwise.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
