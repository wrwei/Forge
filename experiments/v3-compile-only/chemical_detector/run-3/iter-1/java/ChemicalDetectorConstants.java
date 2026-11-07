package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/** Configuration constants fixed at system start-up. */
public final class ChemicalDetectorConstants {
    /** Intensity threshold thr at or above which the source is found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 2.0;
    /** Linear velocity lv for normal travel (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;
    /** Time units spent in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final long EVADE_TIME = 1;
    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final long STUCK_PERIOD = 3;
    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;
    /** Time units spent in GettingOut after shortRandomWalk (CD-Const6). */
    @RoboChartType("nat") public static final long OUT_PERIOD = 2;

    private ChemicalDetectorConstants() {}
}
