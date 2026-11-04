package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants (CD-Const1..6). The requirements fix none of the
 * values; those below are small integral placeholders for start-up
 * configuration. Times are in clock ticks.
 */
public final class ChemicalDetectorConstants {
    /** Intensity threshold at or above which the source is found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 1.0;
    /** Linear velocity of normal travel (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;
    /** Wait in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;
    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;
    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;
    /** Wait in GettingOut after shortRandomWalk (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private ChemicalDetectorConstants() {}
}
