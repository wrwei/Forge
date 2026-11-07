package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system start-up. The requirements give
 * no values; those below are chosen (see post_codegen.md). Whole-valued reals
 * are used so that the model's constants match the Java exactly.
 */
public final class ChemicalDetectorConstants {

    /** thr: intensity at or above which the source is found (intensity units; CD-Const1). */
    @RoboChartType("real") public static final double THR = 1.0;

    /** lv: linear velocity for normal travel (velocity units; CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** evadeTime: wait in Avoiding after changeDirection (time units; CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** stuckPeriod: time window of the stuck-detection rule (time units; CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 3;

    /** stuckDist: distance threshold of the stuck-detection rule (distance units; CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** outPeriod: wait in GettingOut after shortRandomWalk (time units; CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private ChemicalDetectorConstants() {
    }
}
