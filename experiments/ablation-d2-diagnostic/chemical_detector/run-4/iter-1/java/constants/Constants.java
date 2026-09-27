package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants fixed at system start-up (CD-Const1..6). Time values are in
 * the unit of {@link chemical_detector.timing.Clock}.
 */
public final class Constants {

    /** Intensity threshold at or above which the chemical source is found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 5.0;

    /** Linear velocity used for every normal-travel move (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time spent in Avoiding after changeDirection is issued (CD-Const3). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 2;

    /** Stuck-detection time window (CD-Const4). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 5;

    /** Stuck-detection distance threshold (CD-Const5). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 3;

    private Constants() {
    }
}
