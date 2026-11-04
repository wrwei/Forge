package chemical_detector.controller;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup. The requirements name the
 * constants but give no values; the values below are chosen for this implementation.
 */
public final class ChemicalDetectorConstants {

    /** thr: intensity at or above which the chemical source is declared found (Intensity units). */
    @RoboChartType("real")
    public static final double thr = 1.0;

    /** lv: linear velocity of normal travel (arbitrary units). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** evadeTime: time waited in Avoiding after changeDirection (time units). */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** stuckPeriod: time window of the stuck-detection rule (time units). */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** stuckDist: distance to cover between two obstacles to count as escaping (distance units). */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** outPeriod: time spent in GettingOut after shortRandomWalk (time units). */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private ChemicalDetectorConstants() {
    }
}
