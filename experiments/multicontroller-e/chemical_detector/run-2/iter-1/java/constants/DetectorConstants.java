package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at system startup.
 * Durations are expressed in {@link chemical_detector.timing.Clock} time units.
 */
public final class DetectorConstants {

    /** thr: intensity at or above which the chemical source is declared found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 2.0;

    /** lv: linear velocity used for every move during normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** evadeTime: wait inside Avoiding after issuing changeDirection (CD-Const3). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** stuckPeriod: time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 2;

    /** stuckDist: distance the robot must exceed between obstacles to count as escaped (CD-Const5). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** outPeriod: duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    private DetectorConstants() {
    }
}
