package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants fixed at system startup. Durations are in the time unit
 * of {@link chemical_detector.timing.Clock}.
 */
public final class Constants {

    /** Intensity at or above which the chemical source is considered found. */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity used for every motion command during normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time spent in Avoiding after a changeDirection command. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window within which a second obstacle still counts as progress. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 2;

    /** Distance that must be covered between obstacles to count as progress. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut performing the recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    private Constants() {
    }
}
