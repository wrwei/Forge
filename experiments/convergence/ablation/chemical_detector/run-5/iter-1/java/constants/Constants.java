package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * System constants configured at startup (CD-Const1..6).
 */
public final class Constants {

    /** Intensity threshold for declaring the source found (CD-Const1). */
    @RoboChartType("real")
    public static final double thr = 1.0;

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Wait inside Avoiding after a changeDirection command (CD-Const3). */
    @RoboChartType("nat")
    public static final int evadeTime = 1;

    /** Time-window threshold for stuck detection (CD-Const4). */
    @RoboChartType("nat")
    public static final int stuckPeriod = 1;

    /** Distance threshold for stuck detection (CD-Const5). */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int outPeriod = 1;

    private Constants() {
    }
}
