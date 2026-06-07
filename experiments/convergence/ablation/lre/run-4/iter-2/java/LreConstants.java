package lre;

import lre.annotation.RoboChartType;

/**
 * Constant safety thresholds of the AUV (LRE-DM4).
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle (metres). */
    @RoboChartType("real")
    public static final double minSafeDist = 1.0;

    /** Horizontal distance threshold to a static obstacle (metres). */
    @RoboChartType("real")
    public static final double staticObsHorizDist = 1.0;

    /** Vertical distance threshold to a static obstacle (metres). */
    @RoboChartType("real")
    public static final double staticObsVertDist = 1.0;

    /** Default vertical distance threshold to a static obstacle (metres). */
    @RoboChartType("real")
    public static final double staticObsDfltVertDist = 1.0;

    private LreConstants() {
    }
}
