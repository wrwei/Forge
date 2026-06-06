package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Intensity;

/**
 * System-wide constants. Values are configured at startup and do not
 * change at run time.
 */
public final class Constants {

    /** Intensity threshold for declaring a chemical source found. */
    public static final Intensity thr = new Intensity(10.0);

    /** Linear velocity for normal travel (real). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Time spent in the Avoiding state after issuing changeDirection. */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** Time-window threshold for stuck detection. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** Distance threshold for stuck detection (real). */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the shortRandomWalk recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private Constants() {
    }
}
