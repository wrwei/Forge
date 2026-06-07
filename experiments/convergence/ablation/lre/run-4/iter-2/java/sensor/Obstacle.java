package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle known to the AUV (LRE-DM2). All distances are metres and all
 * velocities are m/s, relative to the AUV where named "rel".
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
