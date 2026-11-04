package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system start-up. The requirements give
 * no values; the values below are configuration defaults, chosen as whole
 * numbers so that the model's integer rounding does not change them.
 */
public final class DetectorConstants {

    /** Intensity threshold at or above which the chemical source is found. */
    @RoboChartType("real") public static final double THR = 5.0;

    /** Linear velocity of normal travel (arbitrary units). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Velocity that halts the Vehicle (zero). */
    @RoboChartType("real") public static final double HALT_VELOCITY = 0.0;

    /** Time waited in Avoiding after changeDirection (time units). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (time units). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;

    /** Distance threshold of the stuck-detection rule (distance units). */
    @RoboChartType("real") public static final double STUCK_DIST = 2.0;

    /** Time spent in GettingOut after shortRandomWalk (time units). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private DetectorConstants() {}
}
