package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * Configured constants of the Chemical Detector (CD-Const1..6). Values are
 * fixed at system startup.
 */
public final class DetectorConstants {

    /** Intensity threshold for declaring the chemical source found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity used during normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Duration spent inside Avoiding after a changeDirection command (CD-Const3). */
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

    private DetectorConstants() {
    }
}
