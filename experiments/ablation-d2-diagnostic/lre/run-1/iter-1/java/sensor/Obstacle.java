package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * A single obstacle known to the AUV (LRE-DM2). An obstacle is static when
 * both horizontal velocity components are zero, and dynamic otherwise.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
