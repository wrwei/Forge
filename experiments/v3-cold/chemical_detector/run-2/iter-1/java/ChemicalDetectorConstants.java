package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at start-up.
 * Times are in clock ticks; distances and velocities in arbitrary units.
 */
public final class ChemicalDetectorConstants {

    /** Intensity threshold at or above which the chemical source is found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 10.0;

    /** Linear velocity of normal travel, first argument of every move (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Time waited in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 3;

    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after the short random walk (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 2;

    private ChemicalDetectorConstants() {}
}
