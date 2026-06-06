package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Intensity;

/**
 * Configured at system startup; values do not change at run time.
 */
public final class Constants {

    /** CD-Const1 — intensity threshold for declaring a chemical source 'found'. */
    public static final Intensity thr = new Intensity(50.0);

    /** CD-Const2 — linear velocity for normal travel. */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** CD-Const3 — wait duration inside Avoiding after changeDirection. */
    @RoboChartType("nat")
    public static final int evadeTime = 5;

    /** CD-Const4 — time-window threshold for stuck detection. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 10;

    /** CD-Const5 — distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** CD-Const6 — duration of the recovery shortRandomWalk. */
    @RoboChartType("nat")
    public static final int outPeriod = 5;

    private Constants() {
    }
}
