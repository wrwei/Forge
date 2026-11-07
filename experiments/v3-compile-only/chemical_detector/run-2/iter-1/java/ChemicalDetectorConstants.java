package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Configuration constants, fixed at system startup. The requirements give no
 * values; the values below are placeholders (see post_codegen.md). Durations
 * are in clock ticks of one second.
 */
public final class ChemicalDetectorConstants {
    /** Intensity threshold for declaring the source found (CD-Const1). */
    @RoboChartType("real") public static final double THR = 2.0;

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real") public static final double LV = 1.0;

    /** Time waited in Avoiding after changeDirection (CD-Const3). */
    @RoboChartType("nat") public static final int EVADE_TIME = 1;

    /** Time window of the stuck-detection rule (CD-Const4). */
    @RoboChartType("nat") public static final int STUCK_PERIOD = 2;

    /** Distance threshold of the stuck-detection rule (CD-Const5). */
    @RoboChartType("real") public static final double STUCK_DIST = 1.0;

    /** Time spent in GettingOut after shortRandomWalk (CD-Const6). */
    @RoboChartType("nat") public static final int OUT_PERIOD = 1;

    private ChemicalDetectorConstants() {
    }
}
