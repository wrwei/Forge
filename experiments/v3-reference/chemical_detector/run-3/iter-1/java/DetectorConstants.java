package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector. The requirements fix no
 * values; these are startup defaults (see post_codegen.md). Distances and
 * velocities are in arbitrary units, times in clock ticks.
 */
public final class DetectorConstants {

    /** Intensity threshold at or above which the source is found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 2.0;

    /** Linear velocity of normal travel (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Time spent in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 3;

    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after shortRandomWalk (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 2;

    private DetectorConstants() {
    }
}
