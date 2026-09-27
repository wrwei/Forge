package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * A single obstacle known to the AUV (LRE-DM2). All distances are in metres
 * and all velocities in m/s. An obstacle is static when both of its
 * horizontal velocity components are zero.
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {

    /** True when the obstacle has no horizontal motion. */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }
}
