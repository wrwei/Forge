package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/** Configuration constants of the Chemical Detector, fixed at system startup. */
public final class DetectorConstants {

    /** Intensity threshold at or above which the chemical source is declared found. */
    @RoboChartType("real")
    public static final double thr = 3.0;

    /** Linear velocity used for every move during normal travel. */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Time waited in Avoiding after changeDirection is issued. */
    @RoboChartType("nat")
    public static final int evadeTime = 1;

    /** Time window of the stuck-detection rule. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 2;

    /** Distance threshold of the stuck-detection rule. */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int outPeriod = 1;

    private DetectorConstants() {
    }
}
