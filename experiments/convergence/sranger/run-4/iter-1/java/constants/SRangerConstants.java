package sranger.constants;

/**
 * Fixed configuration constants of the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    /** Linear velocity command when Moving (m/s). */
    public static final double MOVE_VEL = 1.0;

    /** Angular velocity command when Turning (rad/s). */
    public static final double TURN_VEL = 2.0;

    /** IR-distance threshold for obstacle detection (metres). */
    public static final double OBSTACLE_THRESHOLD = 0.5;

    /** Time the controller remains in Turning before returning to Moving (seconds). */
    public static final double TURN_DURATION = 2.0;

    private SRangerConstants() {
    }
}
