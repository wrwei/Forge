package chemical_detector.controller;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup. The requirements give no
 * values; these are chosen small (in arbitrary units and time units).
 */
public final class DetectorConstants {

    /** CD-Const1: intensity threshold thr for declaring the source found. */
    @RoboChartType("real") public static final double THR = 1.0;

    /** CD-Const2: linear velocity lv of normal travel. */
    @RoboChartType("real") public static final double LV = 1.0;

    /** CD-Const3: time evadeTime waited in Avoiding after changeDirection. */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** CD-Const4: time window stuckPeriod of the stuck-detection rule. */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;

    /** CD-Const5: distance threshold stuckDist of the stuck-detection rule. */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** CD-Const6: time outPeriod spent in GettingOut. */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private DetectorConstants() {
    }
}
