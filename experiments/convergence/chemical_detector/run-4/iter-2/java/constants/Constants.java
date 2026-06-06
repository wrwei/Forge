package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * System-wide configuration constants, fixed at startup.
 */
public final class Constants {

    /** Intensity threshold for declaring the chemical source found. */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity used during normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Duration spent in Avoiding after a changeDirection command. */
    public static final int EVADE_TIME = 1;

    /** Time-window threshold for stuck detection. */
    public static final int STUCK_PERIOD = 1;

    /** Distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    public static final int OUT_PERIOD = 1;

    private Constants() {
    }
}
