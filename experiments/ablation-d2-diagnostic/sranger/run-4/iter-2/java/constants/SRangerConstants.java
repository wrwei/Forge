package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants of the SRanger controller.
 */
public final class SRangerConstants {

    /** Linear velocity commanded while Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /** Angular velocity commanded while Turning (rad/s). */
    @RoboChartType("real")
    public static final double TURN_VEL = 2.0;

    /** IR-distance threshold for obstacle detection (m). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time the controller remains in Turning before returning to Moving (s). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {
    }
}
