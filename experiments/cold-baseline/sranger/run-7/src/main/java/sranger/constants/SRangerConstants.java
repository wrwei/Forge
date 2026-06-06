package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants for the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    @RoboChartType("real")
    public static final double moveVel = 1.0;

    @RoboChartType("real")
    public static final double turnVel = 2.0;

    @RoboChartType("real")
    public static final double obstacleThreshold = 0.5;

    @RoboChartType("real")
    public static final double turnDuration = 2.0;

    private SRangerConstants() {
    }
}
