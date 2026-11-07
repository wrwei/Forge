package lre.data;

import lre.annotation.RoboChartType;

/**
 * An obstacle observed by the AUV (LRE-DM2). Positions are relative to the
 * AUV, in metres; velocities are the obstacle's own, in m/s. An obstacle is
 * static when both horizontal velocity components are zero, dynamic otherwise.
 *
 * @param ns_rel_dist relative distance north-south (m)
 * @param ew_rel_dist relative distance east-west (m)
 * @param obs_depth   obstacle depth (m)
 * @param obs_ns_vel  obstacle velocity north-south (m/s)
 * @param obs_ew_vel  obstacle velocity east-west (m/s)
 * @param obs_roc     obstacle rate of climb (m/s)
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {
}
