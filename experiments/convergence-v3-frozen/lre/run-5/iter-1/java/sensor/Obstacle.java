package lre.sensor;

import lre.annotation.RoboChartType;

/** An obstacle as reported by the AUV's mapping system, relative to the AUV. */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {

    /** An obstacle is static when both horizontal velocity components are zero. */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }
}
