package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Constants of the AUV and the LRE.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, in metres (LRE-DM4). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, in metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, in metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, in metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Distance, in metres, the sensor reports when there is no obstacle (LRE-DM5). */
    @RoboChartType("real") public static final double SAFE_LARGE_DIST = 1000000.0;

    /**
     * Obstacle index meaning "no such obstacle" (LRE-Var5, LRE-Var6).
     * Indices are natural numbers, so the register never uses this index.
     */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;

    /** Added to the CPA denominator so it is never zero, in m^2/s^2 (LRE-OP5). */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
