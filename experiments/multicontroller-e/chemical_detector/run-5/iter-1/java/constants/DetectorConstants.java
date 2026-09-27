package chemical_detector.constants;

import chemical_detector.annotation.RoboChartType;

/** Configuration constants fixed at system startup (CD-Const1..6). */
public final class DetectorConstants {

    /** thr: intensity at or above which the chemical source is found (CD-Const1). */
    @RoboChartType("real")
    public static final double THR = 1.0;

    /** lv: linear velocity for every move during normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** Zero velocity: commanding it facing Front halts the Vehicle (CD-OP1, CD-MV-FR3). */
    @RoboChartType("real")
    public static final double HALT_VELOCITY = 0.0;

    /** evadeTime: wait inside Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat")
    public static final long EVADE_TIME = 1;

    /** stuckPeriod: time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat")
    public static final long STUCK_PERIOD = 1;

    /** stuckDist: distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** outPeriod: duration of the GettingOut recovery wait (CD-Const6). */
    @RoboChartType("nat")
    public static final long OUT_PERIOD = 1;

    private DetectorConstants() {
    }
}
