package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle observed by the AUV (LRE-DM2). Distances are relative to
 * the AUV in metres; velocities are in m/s.
 */
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

    /** An obstacle is dynamic when it is not static. */
    public boolean isDynamic() {
        return !isStatic();
    }
}
