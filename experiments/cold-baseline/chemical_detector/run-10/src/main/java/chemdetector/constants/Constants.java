package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Intensity;

/**
 * System-wide configuration constants (CD-Const1..6).
 */
public final class Constants {

    /** CD-Const1: intensity threshold for declaring the chemical found. */
    public static final Intensity thr = new Intensity(10.0);

    /** CD-Const2: linear velocity during normal travel. */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** CD-Const3: time spent in the Avoiding state after a changeDirection. */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** CD-Const4: stuck-detection time window. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** CD-Const5: stuck-detection distance threshold. */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** CD-Const6: time spent inside GettingOut performing shortRandomWalk. */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private Constants() {
    }
}
