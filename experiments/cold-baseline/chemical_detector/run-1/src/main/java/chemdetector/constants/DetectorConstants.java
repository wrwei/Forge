package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Intensity;

/**
 * CD-Const1..6: system-wide constants configured at startup.
 */
public final class DetectorConstants {

    /** CD-Const1: intensity threshold for declaring the chemical source found. */
    public static final Intensity thr = new Intensity(10.0);

    /** CD-Const2: linear velocity used by move(lv, a). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** CD-Const3: time spent inside the Avoiding state after changeDirection. */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** CD-Const4: time window threshold for stuck-detection. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** CD-Const5: distance threshold for stuck-detection. */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** CD-Const6: bounded recovery period (GettingOut). */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private DetectorConstants() {
    }
}
