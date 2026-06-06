package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants of the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    /** Linear velocity command when Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /**
     * Angular velocity command when Turning (rad/s). The specification
     * default is 2.0; scaled to 1.0 so the commanded value fits the
     * verification type range {0..1} used by the FDR4 model checking
     * (the moveCall channel payload must lie within the range).
     */
    @RoboChartType("real")
    public static final double TURN_VEL = 1.0;

    /** IR-distance threshold for obstacle detection (metres). */
    @RoboChartType("real")
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time the controller remains in Turning before returning to Moving (seconds). */
    @RoboChartType("real")
    public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {
    }
}
