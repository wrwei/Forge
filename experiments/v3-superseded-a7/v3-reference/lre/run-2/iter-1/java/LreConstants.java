package lre;

import lre.annotation.RoboChartType;

/**
 * Constant thresholds of the AUV (LRE-DM4). Distances in metres.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /**
     * Added to the squared closing speed in CalcCPA so that tcpa is defined
     * when the AUV and the obstacle do not close on each other, (m/s)^2.
     */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
