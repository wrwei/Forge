package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at startup (CD-Const1..6).
 * Times are in clock ticks, distances and velocities in the odometer's units.
 */
public final class ChemicalDetectorConstants {

    /** Intensity threshold at or above which the source counts as found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 3.0;

    /** Linear velocity of normal travel (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Time waited in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 2;

    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 4;

    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after shortRandomWalk (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 2;

    private ChemicalDetectorConstants() {
    }
}
