package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed for a run.
 * Durations are in the units of {@link chemical_detector.sensor.Clock}.
 */
public final class Constants {

    /** Intensity threshold at or above which the chemical source is found. */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity used for every move during normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time waited in Avoiding after issuing changeDirection. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 1;

    /** Distance threshold of the stuck-detection rule. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after the shortRandomWalk. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    private Constants() {
    }
}
