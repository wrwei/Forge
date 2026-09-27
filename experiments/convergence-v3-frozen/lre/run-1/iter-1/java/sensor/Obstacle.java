package lre.sensor;

import lre.annotation.RoboChartType;

/** A single obstacle, described relative to the AUV. */
public record Obstacle(
        @RoboChartType("real") double ns_rel_dist,
        @RoboChartType("real") double ew_rel_dist,
        @RoboChartType("real") double obs_depth,
        @RoboChartType("real") double obs_ns_vel,
        @RoboChartType("real") double obs_ew_vel,
        @RoboChartType("real") double obs_roc) {

    /** An obstacle is static when both horizontal velocity components are zero. */
    public boolean isStatic() {
        return obs_ns_vel == 0.0 && obs_ew_vel == 0.0;
    }

    /** An obstacle is dynamic when it is not static. */
    public boolean isDynamic() {
        return obs_ns_vel != 0.0 || obs_ew_vel != 0.0;
    }
}
