package lre.constants;

import lre.annotation.RoboChartType;

/**
 * LRE-DM4: AUV safety thresholds.
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
