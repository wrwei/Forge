package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/** System configuration fixed at startup (CD-Const1 .. CD-Const6). */
public final class ChemicalDetectorConstants {

    /** Intensity threshold for declaring the chemical source found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 5.0;

    /** Linear velocity used for normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Time spent in Avoiding while changeDirection completes (CD-Const3). */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 2;

    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 10;

    /** Distance the robot must cover between obstacles to count as escaping (CD-Const5). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** Duration of the shortRandomWalk recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 3;

    private ChemicalDetectorConstants() {
    }
}
