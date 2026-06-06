package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Obstacle record (LRE-DM2). An obstacle is static iff both obs_ns_vel and obs_ew_vel are zero.
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
