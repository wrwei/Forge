package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * One obstacle known to the AUV (LRE-DM2). Positions are relative to the AUV;
 * an obstacle whose north-south and east-west velocities are both zero is
 * static, otherwise it is dynamic.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
