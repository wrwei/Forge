package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at system startup.
 */
public final class ChemConstants {

    /** Intensity threshold at or above which the chemical source is declared found (thr). */
    @RoboChartType("real")
    public static final double THR = 3.0;

    /** Linear velocity used for every move during normal travel (lv). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time waited in Avoiding after issuing changeDirection (evadeTime). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (stuckPeriod). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 3;

    /** Distance threshold of the stuck-detection rule (stuckDist). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after the recovery random walk (outPeriod). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 2;

    private ChemConstants() {
    }
}
