package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/** Configuration constants of the Chemical Detector, fixed at system startup. Times are in clock units. */
public final class DetectorConstants {

    /** thr: peak intensity at or above which the chemical source is declared found. */
    @RoboChartType("real")
    public static final double THR = 5.0;

    /** lv: linear velocity used for every move during normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** evadeTime: time waited in Avoiding after issuing changeDirection. */
    @RoboChartType("nat")
    public static final long EVADE_TIME = 1;

    /** stuckPeriod: time window of the stuck-detection rule. */
    @RoboChartType("nat")
    public static final long STUCK_PERIOD = 3;

    /** stuckDist: distance that must be exceeded between obstacles to count as progress. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 2.0;

    /** outPeriod: time spent in GettingOut after the short random walk. */
    @RoboChartType("nat")
    public static final long OUT_PERIOD = 2;

    private DetectorConstants() {
    }
}
