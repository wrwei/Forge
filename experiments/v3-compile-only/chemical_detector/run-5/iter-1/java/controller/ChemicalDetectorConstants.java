package chemical_detector.controller;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup. The requirements give no
 * values; the values below are deployment defaults (integral, so the model's
 * constants interface represents them exactly).
 */
public final class ChemicalDetectorConstants {

    /** Intensity threshold at or above which the source counts as found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 2.0;

    /** Linear velocity for normal travel, in velocity units (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Time waited in Avoiding after changeDirection, in time units (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Stuck-detection time window, in time units (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;

    /** Stuck-detection distance threshold, in distance units (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after shortRandomWalk, in time units (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private ChemicalDetectorConstants() {
    }
}
