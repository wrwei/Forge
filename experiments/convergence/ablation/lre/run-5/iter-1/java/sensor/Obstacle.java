package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle relative to the AUV. An obstacle is static if both
 * obsNsVel and obsEwVel are zero; otherwise it is dynamic.
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {
}
