package lre.constants;

import lre.annotation.RoboChartType;

/**
 * AUV safety threshold constants (LRE-DM4). Field names follow the
 * requirement names verbatim for model traceability.
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

    private LreConstants() {
    }
}
