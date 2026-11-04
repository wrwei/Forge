package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants of the SRanger controller.
 */
public final class SRangerConstants {

    /** moveVel: linear velocity commanded in Moving, m/s. */
    @RoboChartType("real") public static final double MOVE_VEL = 1.0;

    /** turnVel: angular velocity commanded in Turning, rad/s. */
    @RoboChartType("real") public static final double TURN_VEL = 2.0;

    /** obstacleThreshold: IR distance at or below which an obstacle is detected, metres. */
    @RoboChartType("real") public static final double OBSTACLE_THRESHOLD = 0.5;

    /** turnDuration: time spent in Turning before returning to Moving, seconds. */
    @RoboChartType("real") public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {}
}
