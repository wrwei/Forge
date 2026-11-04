package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle: position relative to the AUV, depth, and velocity.
 * It is static when both horizontal velocity components are zero.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
