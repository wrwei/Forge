package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * A single obstacle, given relative to the AUV. An obstacle is static when both
 * horizontal velocity components are zero, and dynamic otherwise.
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {

    /** Whether this obstacle has no horizontal motion. */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }

    /** Whether this obstacle has horizontal motion. */
    public boolean isDynamic() {
        return !isStatic();
    }
}
