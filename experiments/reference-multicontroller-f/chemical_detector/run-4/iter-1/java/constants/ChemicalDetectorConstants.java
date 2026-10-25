package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup. Durations are in Clock time units.
 */
public final class ChemicalDetectorConstants {

    /** Intensity threshold at or above which the chemical source is considered found. */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity for normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Wait inside Avoiding after issuing changeDirection. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time window for stuck detection. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 1;

    /** Distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    /** Velocity that halts the Vehicle. */
    @RoboChartType("real")
    public static final double HALT_VELOCITY = 0.0;

    private ChemicalDetectorConstants() {
    }
}
