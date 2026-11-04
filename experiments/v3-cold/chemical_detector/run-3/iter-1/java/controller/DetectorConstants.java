package chemical_detector.controller;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at start-up. The requirements give no
 * values; the values below are chosen defaults. Times are in clock ticks.
 */
public final class DetectorConstants {

    /** Intensity threshold for declaring the source found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 2.0;

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Wait in Avoiding after steering away (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 3;

    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery wait (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 2;

    private DetectorConstants() {}
}
