package chemdetector.data;

import chemdetector.annotation.RoboChartType;

/**
 * Configuration constants of the Chemical Detector, fixed at system
 * startup.
 */
public final class CdConstants {

    /** Intensity threshold for declaring the chemical source found. */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity used during normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Duration to wait inside Avoiding after a changeDirection command. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time-window threshold for the stuck-detection rule. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 1;

    /** Distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    /** Identity of the target chemical species. */
    @RoboChartType("nat")
    public static final int TARGET_CHEM = 1;

    private CdConstants() {
    }
}
