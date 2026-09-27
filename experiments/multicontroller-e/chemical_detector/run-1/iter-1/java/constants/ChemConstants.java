package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants fixed at system start-up. Durations are expressed in
 * the time unit of {@link chemical_detector.sensor.Clock}.
 */
public final class ChemConstants {

    /** Intensity at or above which the chemical source counts as found. */
    @RoboChartType("real")
    public static final double THR = 5.0;

    /** Linear velocity used for every move during normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time spent in Avoiding after issuing changeDirection. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 3;

    /** Distance the robot must cover between two obstacles to count as escaped. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 2.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 2;

    private ChemConstants() {
    }
}
