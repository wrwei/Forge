package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle detected near the AUV (LRE-DM2). All distances are metres and
 * all velocities metres per second. An obstacle is static when both
 * horizontal velocity components are zero, and dynamic otherwise.
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {
}
