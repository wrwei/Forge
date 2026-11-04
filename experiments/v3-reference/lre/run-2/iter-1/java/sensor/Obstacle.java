package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle seen by the AUV (LRE-DM2). Distances in metres, velocities in m/s.
 *
 * @param ns_rel_dist relative distance north-south
 * @param ew_rel_dist relative distance east-west
 * @param obs_depth   obstacle depth
 * @param obs_ns_vel  obstacle velocity north-south
 * @param obs_ew_vel  obstacle velocity east-west
 * @param obs_roc     obstacle rate of climb
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {

    /** An obstacle is static when both of its horizontal velocity components are zero. */
    public boolean isStatic() {
        return obs_ns_vel == 0.0 && obs_ew_vel == 0.0;
    }
}
