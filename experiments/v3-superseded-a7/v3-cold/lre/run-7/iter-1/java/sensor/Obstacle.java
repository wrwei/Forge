package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle as seen from the AUV (LRE-DM2). Distances in metres, velocities in m/s.
 * The component names are those of the requirement.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
