package lre.constants;

import lre.annotation.RoboChartType;

/**
 * Safety thresholds for the LRE (LRE-DM4).
 */
public final class LreConstants {

    @RoboChartType("real")
    public static final double minSafeDist = 1.0;

    @RoboChartType("real")
    public static final double staticObsHorizDist = 1.0;

    @RoboChartType("real")
    public static final double staticObsVertDist = 1.0;

    @RoboChartType("real")
    public static final double staticObsDfltVertDist = 1.0;

    private LreConstants() {}
}
