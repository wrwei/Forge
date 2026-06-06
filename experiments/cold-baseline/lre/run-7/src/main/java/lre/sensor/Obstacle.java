package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle observed by the AUV's sensor system (LRE-DM2).
 * Fields:
 *  - ns_rel_dist: north-south relative distance (m)
 *  - ew_rel_dist: east-west relative distance (m)
 *  - obs_depth:   obstacle depth (m)
 *  - obs_ns_vel:  obstacle north-south velocity (m/s)
 *  - obs_ew_vel:  obstacle east-west velocity (m/s)
 *  - obs_roc:     obstacle rate of climb (m/s)
 *
 * An obstacle is static iff both obs_ns_vel and obs_ew_vel are zero.
 */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {

    public boolean isStatic() {
        return obs_ns_vel == 0.0 && obs_ew_vel == 0.0;
    }

    public boolean isDynamic() {
        return !isStatic();
    }
}
