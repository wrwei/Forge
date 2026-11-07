package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants of the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    /** Linear velocity commanded in Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /** Angular velocity commanded in Turning (rad/s). */
    @RoboChartType("real")
    public static final double TURN_VEL = 2.0;

    /** IR distance at or below which an obstacle is detected (metres). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time the controller stays in Turning before returning to Moving (seconds). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {}
}
