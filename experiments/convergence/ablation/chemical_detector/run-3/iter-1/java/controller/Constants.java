package chemdetector.controller;

import chemdetector.annotation.RoboChartType;

/** Configuration constants, fixed at system startup (CD-Const1..CD-Const6). */
public final class Constants {

    /** Intensity threshold for declaring the chemical source found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity during normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Wait inside Avoiding after a changeDirection command (CD-Const3). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 1;

    /** Time-window threshold for stuck detection (CD-Const4). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 1;

    /** Distance threshold for stuck detection (CD-Const5). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 1;

    private Constants() {
    }
}
