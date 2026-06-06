package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants for the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    /** Linear velocity command when Moving (m/s). */
    @RoboChartType("real")
    public static final double MOVE_VEL = 1.0;

    /**
     * Angular velocity command when Turning (rad/s). SR-DM2 specifies
     * 2.0; scaled to 1.0 because the FDR4 verification type ranges
     * clamp channel payloads to {0..1} and the value is carried on the
     * moveCall channel. The Move(0, turnVel) / Move(moveVel, 0)
     * commands remain distinguishable by argument position.
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
