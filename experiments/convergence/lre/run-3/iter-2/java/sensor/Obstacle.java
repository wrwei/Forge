package lre.sensor;

import lre.annotation.RoboChartType;

/**
 * An obstacle observed by the AUV (LRE-DM2).
 *
 * @param nsRelDist relative distance north-south (metres)
 * @param ewRelDist relative distance east-west (metres)
 * @param obsDepth  obstacle depth (metres)
 * @param obsNsVel  obstacle velocity north-south (m/s)
 * @param obsEwVel  obstacle velocity east-west (m/s)
 * @param obsRoc    obstacle rate of climb (m/s)
 */
public record Obstacle(
        @RoboChartType("real") double nsRelDist,
        @RoboChartType("real") double ewRelDist,
        @RoboChartType("real") double obsDepth,
        @RoboChartType("real") double obsNsVel,
        @RoboChartType("real") double obsEwVel,
        @RoboChartType("real") double obsRoc) {

    /** An obstacle is static if both horizontal velocity components are zero. */
    public boolean isStatic() {
        return obsNsVel == 0.0 && obsEwVel == 0.0;
    }

    /** An obstacle is dynamic if it is not static. */
    public boolean isDynamic() {
        return !isStatic();
    }
}
