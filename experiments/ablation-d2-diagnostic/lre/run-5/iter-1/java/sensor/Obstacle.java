package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * A single obstacle known to the AUV, expressed relative to the AUV.
 * An obstacle is static when both horizontal velocity components are zero.
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
        return obsNsVel != 0.0 || obsEwVel != 0.0;
    }
}
