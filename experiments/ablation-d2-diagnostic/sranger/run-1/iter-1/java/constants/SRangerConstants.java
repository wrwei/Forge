package sranger.constants;

import sranger.annotation.RoboChartType;

/** Fixed configuration constants of the SRanger controller. */
public final class SRangerConstants {

    /** Linear velocity commanded in Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /** Angular velocity commanded in Turning (rad/s). */
    @RoboChartType("real")
    public static final double TURN_VEL = 2.0;

    /** IR distance at or below which an obstacle is detected (m). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time spent in Turning before returning to Moving (s). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    /** TURN_DURATION expressed in the controller clock's unit (ms). */
    @RoboChartType("real")
    public static final double TURN_DURATION_MS = 2000.0;

    private SRangerConstants() {
    }
}
