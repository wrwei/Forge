package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle observed by the AUV's sensors. Distances are relative to the
 * AUV in metres; velocities are in m/s.
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {

    /**
     * An obstacle is static if both its horizontal velocity components are
     * zero; otherwise it is dynamic.
     */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }
}
