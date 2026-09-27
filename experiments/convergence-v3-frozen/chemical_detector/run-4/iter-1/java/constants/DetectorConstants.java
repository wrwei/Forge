package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at system startup.
 * Time quantities are expressed in abstract clock units (see {@code Clock}).
 */
public final class DetectorConstants {

    /** Intensity threshold at or above which the chemical source is declared found (thr). */
    @RoboChartType("real")
    public static final double THR = 3.0;

    /** Linear velocity used for every normal-travel move (lv). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time waited in Avoiding after issuing changeDirection (evadeTime). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Stuck-detection time window measured from the start of an evasion (stuckPeriod). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 2;

    /** Stuck-detection distance threshold between two obstacles (stuckDist). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after the short random walk (outPeriod). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    private DetectorConstants() {
    }
}
