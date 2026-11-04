package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle seen by the AUV (LRE-DM2). Distances are relative to the AUV
 * (obstacle position minus AUV position), in metres; velocities in m/s.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {

    /** An obstacle is static when both horizontal velocity components are zero; otherwise dynamic. */
    public boolean isStatic() {
        return obs_ns_vel == 0.0 && obs_ew_vel == 0.0;
    }
}
