package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * SR-DM2: fixed configuration constants for the SRanger controller.
 *
 *  - moveVel:           linear velocity command while Moving (m/s)
 *  - turnVel:           angular velocity command while Turning (rad/s)
 *  - obstacleThreshold: IR-distance threshold for obstacle detection (m)
 *  - turnDuration:      how long the controller remains in Turning (s)
 */
public final class SRangerConstants {

    @RoboChartType("real")
    public static final double moveVel = 1.0;

    @RoboChartType("real")
    public static final double turnVel = 2.0;

    @RoboChartType("real")
    public static final double obstacleThreshold = 0.5;

    @RoboChartType("real")
    public static final double turnDuration = 2.0;

    private SRangerConstants() {
        // utility holder
    }
}
