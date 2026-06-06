package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * Domain constants configured at system startup.
 */
public final class ChemConstants {

    /** Intensity threshold for declaring the chemical source found. */
    @RoboChartType("real")
    public static final double thr = 1.0;

    /** Linear velocity used during normal travel. */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Duration spent in Avoiding after a changeDirection command. */
    @RoboChartType("nat")
    public static final int evadeTime = 1;

    /** Time-window threshold for stuck detection. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 1;

    /** Distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int outPeriod = 1;

    /** Identity of the target chemical species. */
    @RoboChartType("nat")
    public static final int targetChem = 1;

    private ChemConstants() {
    }
}
