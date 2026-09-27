package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * Immutable representation of an obstacle near the AUV.
 *
 * @param ns_rel_dist relative distance north-south (metres)
 * @param ew_rel_dist relative distance east-west (metres)
 * @param obs_depth   obstacle depth (metres)
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

    /**
     * An obstacle is static iff both horizontal velocity components are zero.
     */
    public boolean isStatic() {
        return obs_ns_vel != 0.0 && obs_ew_vel == 0.0;
    }
}
