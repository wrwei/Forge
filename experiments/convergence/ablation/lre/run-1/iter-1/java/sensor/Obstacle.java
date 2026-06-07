package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle observed by the AUV (LRE-DM2). Distances are relative to
 * the AUV; an obstacle is static when both velocity components are zero.
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {

    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }

    public boolean isDynamic() {
        return !isStatic();
    }
}
