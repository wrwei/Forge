package chemdetector.controller;

import chemdetector.annotation.RoboChartType;

/**
 * Configured-at-startup constants (CD-Const1..6). Field names follow the
 * requirement names for traceability into the formal model.
 */
public final class Constants {

    /** Intensity threshold for declaring the source found (CD-Const1). */
    @RoboChartType("real")
    public static final double thr = 1.0;

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Wait inside Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** Time-window threshold for stuck detection (CD-Const4). */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** Distance threshold for stuck detection (CD-Const5). */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private Constants() {
    }
}
