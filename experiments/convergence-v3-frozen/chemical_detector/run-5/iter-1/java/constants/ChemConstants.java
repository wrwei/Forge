package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at system start-up.
 * Time-valued constants are expressed in {@link chemical_detector.timing.Clock}
 * time units.
 */
public final class ChemConstants {

    /** Intensity at or above which the chemical source is considered found (thr). */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity used for every move during normal travel (lv). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time waited in Avoiding after issuing changeDirection (evadeTime). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (stuckPeriod). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 1;

    /** Distance that must be covered between two obstacles to count as escaped (stuckDist). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after the short random walk (outPeriod). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    private ChemConstants() {
    }
}
