package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system start-up (CD-Const1 to CD-Const6).
 * Time is in control cycles; distance and velocity are in the Vehicle's arbitrary units.
 */
public final class ChemicalDetectorConstants {

    /** CD-Const1: peak intensity at or above which the chemical source is found. */
    @RoboChartType("nat") public static final int THR = 3;

    /** CD-Const2: linear velocity of normal travel; first argument of every move. */
    @RoboChartType("real") public static final double LV = 1.0;

    /** CD-Const3: time waited in Avoiding after changeDirection. */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** CD-Const4: time window of the stuck-detection rule. */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 3;

    /** CD-Const5: distance between two obstacles above which the robot has escaped. */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** CD-Const6: time spent in GettingOut after the short random walk. */
    @RoboChartType("nat") public static final int OUT_PERIOD = 2;

    private ChemicalDetectorConstants() {
    }
}
