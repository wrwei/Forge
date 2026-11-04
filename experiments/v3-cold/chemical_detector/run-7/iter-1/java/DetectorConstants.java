package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration fixed at system startup. The requirements give no values; the values
 * below are placeholders (see post_codegen.md).
 */
public final class DetectorConstants {

    /** Intensity threshold (thr) at or above which the chemical source is found. */
    @RoboChartType("real") public static final double THR = 2.0;

    /** Linear velocity (lv) of normal travel, arbitrary units. */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Time (evadeTime) waited in Avoiding after changeDirection, in clock ticks. */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Time window (stuckPeriod) of the stuck-detection rule, in clock ticks. */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 3;

    /** Distance threshold (stuckDist) of the stuck-detection rule, odometer units. */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Duration (outPeriod) of the GettingOut recovery manoeuvre, in clock ticks. */
    @RoboChartType("nat") public static final int OUT_PERIOD = 2;

    private DetectorConstants() {
    }
}
