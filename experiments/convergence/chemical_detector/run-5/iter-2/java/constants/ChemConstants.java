package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * Domain constants for the Chemical Detector (CD-Const1..CD-Const6).
 * Values are configured at system startup and do not change at run
 * time.
 */
public final class ChemConstants {

    /** Intensity threshold for declaring the source found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** Linear velocity during normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Wait inside Avoiding after a changeDirection command (CD-Const3). */
    public static final int EVADE_TIME = 1;

    /** Time window for the stuck-detection rule (CD-Const4). */
    public static final int STUCK_PERIOD = 1;

    /** Distance threshold for stuck detection (CD-Const5). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 0.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    public static final int OUT_PERIOD = 1;

    private ChemConstants() {
    }
}
