package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants of the SRanger controller.
 */
public final class SRangerConstants {

    /** Linear velocity command when Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /** Angular velocity command when Turning (rad/s). */
    @RoboChartType("real")
    public static final double TURN_VEL = 2.0;

    /** IR-distance threshold for obstacle detection (metres). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time spent in Turning before returning to Moving (seconds). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {
    }
}
