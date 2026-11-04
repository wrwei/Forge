package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle seen by the AUV. Distances are relative to the AUV, in metres;
 * velocities are the obstacle's own, in m/s. An obstacle is static when both
 * horizontal velocities are zero, and dynamic otherwise.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
