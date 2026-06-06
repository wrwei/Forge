package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Controller configuration constants (SR-DM2).
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

    /**
     * turnDuration expressed in milliseconds. Used directly as the
     * comparator constant in {@code clock.nowMs() - clockResetTime >= turnDurationMs}
     * so the M2M's {@code since(...)} rewrite recognises the time predicate.
     */
    @RoboChartType("real")
    public static final double turnDurationMs = 2000.0;

    private SRangerConstants() {
        // constants holder
    }
}
