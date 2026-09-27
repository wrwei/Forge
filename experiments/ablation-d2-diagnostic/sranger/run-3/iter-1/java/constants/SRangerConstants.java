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

    /** IR-distance threshold for obstacle detection (metres). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time the controller remains in Turning (seconds). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    /** TURN_DURATION expressed in the Clock's millisecond time base. */
    @RoboChartType("real")
    public static final double TURN_DURATION_MS = 2000.0;

    /** Distance reported by the Sensor when no IR reading is available (metres). */
    @RoboChartType("real")
    public static final double NO_READING_DISTANCE = 1000.0;

    private SRangerConstants() {
    }
}
