package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at system start-up.
 * Durations are expressed in the platform time unit returned by
 * {@code Clock.nowMs()}.
 */
public final class ChemConstants {

    /** Intensity threshold at or above which the chemical source is found. */
    @RoboChartType("real")
    public static final double THR = 3.0;

    /** Linear velocity used for every normal-travel move. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time waited in Avoiding after issuing changeDirection. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 3;

    /** Distance threshold of the stuck-detection rule. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 2;

    private ChemConstants() {
    }
}
