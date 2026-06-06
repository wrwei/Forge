package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants for the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    /** Linear velocity command when Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /** Angular velocity command when Turning (rad/s). */
    @RoboChartType("real")
    public static final double TURN_VEL = 2.0;

    /** IR-distance threshold for the obstacle-detection condition (metres). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** How long the controller remains in Turning before returning to Moving (seconds). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {
    }
}
