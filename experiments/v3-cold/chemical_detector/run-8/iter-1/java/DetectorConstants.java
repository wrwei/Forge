package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/** Configuration constants, fixed at start-up (CD-Const1..6). */
public final class DetectorConstants {
    /** thr: intensity at or above which the chemical source is found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 10.0;

    /** lv: linear velocity of normal travel, arbitrary units (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** evadeTime: time units waited in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 2;

    /** stuckPeriod: time window of the stuck-detection rule, time units (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 5;

    /** stuckDist: distance that must be exceeded to count as escaping, arbitrary units (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** outPeriod: time units spent in GettingOut after a shortRandomWalk (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 3;

    private DetectorConstants() {
    }
}
