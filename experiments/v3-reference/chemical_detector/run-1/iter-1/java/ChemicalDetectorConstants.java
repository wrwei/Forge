package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup.
 */
public final class ChemicalDetectorConstants {
    /** Intensity threshold at or above which the source is found (intensity units). */
    @RoboChartType("real") public static final double THR = 10.0;
    /** Linear velocity of normal travel (arbitrary velocity units). */
    @RoboChartType("real") public static final double LV = 1.0;
    /** Time waited in Avoiding after changeDirection (time units). */
    @RoboChartType("nat") public static final long EVADE_TIME = 2;
    /** Stuck-detection time window since the evasion began (time units). */
    @RoboChartType("nat") public static final long STUCK_PERIOD = 5;
    /** Stuck-detection distance threshold (distance units). */
    @RoboChartType("real") public static final double STUCK_DIST = 2.0;
    /** Time spent in GettingOut after the short random walk (time units). */
    @RoboChartType("nat") public static final long OUT_PERIOD = 3;

    private ChemicalDetectorConstants() {}
}
