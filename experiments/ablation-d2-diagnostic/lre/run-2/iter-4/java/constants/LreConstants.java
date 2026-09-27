package lre.constants;

import lre.annotation.RoboChartType;

/** The AUV safety thresholds (LRE-DM4). */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, in metres. */
    @RoboChartType("real")
    public static final double minSafeDist = 1.0;

    /** Horizontal distance threshold to a static obstacle, in metres. */
    @RoboChartType("real")
    public static final double staticObsHorizDist = 1.0;

    /** Vertical distance threshold to a static obstacle, in metres. */
    @RoboChartType("real")
    public static final double staticObsVertDist = 1.0;

    /** Default vertical distance threshold to a static obstacle, in metres. */
    @RoboChartType("real")
    public static final double staticObsDfltVertDist = 1.0;

    private LreConstants() {
    }
}
