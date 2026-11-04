package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle relative to the AUV (LRE-DM2). Distances in metres, velocities
 * in m/s. Components are named as in the requirement. An obstacle is static
 * when both horizontal velocity components are zero, otherwise dynamic.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
