package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * System-wide constants. Configured at startup; immutable at runtime.
 *
 * - CD-Const1: thr (intensity threshold for declaring 'found')
 * - CD-Const2: lv  (linear velocity for normal travel)
 * - CD-Const3: evadeTime (wait inside Avoiding after changeDirection)
 * - CD-Const4: stuckPeriod (time-window threshold for stuck detection)
 * - CD-Const5: stuckDist (distance threshold for stuck detection)
 * - CD-Const6: outPeriod (duration of the GettingOut recovery walk)
 */
public final class Constants {

    @RoboChartType("real")
    public static final double thr = 5.0;

    @RoboChartType("real")
    public static final double lv = 1.0;

    @RoboChartType("nat")
    public static final int evadeTime = 2;

    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private Constants() {
    }
}
