package lre.constants;

import lre.annotation.RoboChartType;

/**
 * AUV safety thresholds (LRE-DM4).
 * <p>
 * All four defaults are 1.0 as per the requirements.
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
