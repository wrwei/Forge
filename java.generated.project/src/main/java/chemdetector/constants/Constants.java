package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * System configuration constants (CD-Const1..6), fixed at startup.
 */
public final class Constants {

    /** Intensity threshold for declaring the source found (CD-Const1). */
    @RoboChartType("real")
    public static final double thr = 5.0;

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Wait inside Avoiding after a changeDirection command (CD-Const3). */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** Time-window threshold for stuck detection (CD-Const4). */
    @RoboChartType("nat")
    public static final int stuckPeriod = 10;

    /** Distance threshold for stuck detection (CD-Const5). */
    @RoboChartType("real")
    public static final double stuckDist = 3.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int outPeriod = 5;

    private Constants() {
    }
}
