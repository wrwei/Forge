package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup (CD-Const1..6). The
 * requirements give no values; these are placeholders to be configured.
 */
public final class ChemicalDetectorConstants {
    /** Intensity threshold at or above which the source is found (CD-Const1). */
    @RoboChartType("nat") public static final int THR = 2;
    /** Linear velocity of normal travel, arbitrary units (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;
    /** Time waited in Avoiding after changeDirection, time units (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;
    /** Time window of the stuck-detection rule, time units (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;
    /** Distance threshold of the stuck-detection rule, distance units (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;
    /** Time spent in GettingOut after shortRandomWalk, time units (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private ChemicalDetectorConstants() {}
}
