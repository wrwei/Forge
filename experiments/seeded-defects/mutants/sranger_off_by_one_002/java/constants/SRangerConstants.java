package sranger.constants;

import sranger.annotation.RoboChartType;

/**
 * Fixed configuration constants for the SRanger controller (SR-DM2).
 */
public final class SRangerConstants {

    /** Linear velocity command when Moving (m/s). */
    @RoboChartType("real")
    public static final double moveVel = 1.0;

    /** Angular velocity command when Turning (rad/s). */
    @RoboChartType("real")
    public static final double turnVel = 2.0;

    /** IR-distance threshold for obstacle detection (metres). */
    @RoboChartType("real")
    public static final double obstacleThreshold = 1.5;

    /** Duration the controller stays in Turning before returning to Moving (seconds). */
    @RoboChartType("real")
    public static final double turnDuration = 2.0;

    /**
     * Same duration as {@link #turnDuration} but expressed in milliseconds, so that the
     * time-since-clock-reset predicate in the controller stays in the same unit
     * ({@link sranger.timing.Clock#nowMs()}). The M2M clock-pattern matcher requires
     * predicates of the form {@code clock.nowMs() - <clockField> < CONST}; mixing units
     * (e.g. {@code clock.nowMs() / 1000.0}) would defeat the pattern.
     */
    @RoboChartType("real")
    public static final double turnDurationMs = 2000.0;

    private SRangerConstants() {
    }
}
