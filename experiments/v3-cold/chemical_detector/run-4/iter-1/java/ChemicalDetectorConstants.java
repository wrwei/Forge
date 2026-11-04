package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system start-up.
 */
public final class ChemicalDetectorConstants {
    /** thr: intensity at or above which the chemical source is declared found. */
    @RoboChartType("real") public static final double THR = 2.0;

    /** lv: linear velocity of normal travel (arbitrary units). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** evadeTime: time waited in Avoiding after changeDirection. */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** stuckPeriod: time window of the stuck-detection rule. */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;

    /** stuckDist: distance threshold of the stuck-detection rule. */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** outPeriod: time spent in GettingOut after shortRandomWalk. */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    /** Zero velocity, used with direction Front to halt the Vehicle. */
    @RoboChartType("real") public static final double HALT_VELOCITY = 0.0;

    private ChemicalDetectorConstants() {
    }
}
