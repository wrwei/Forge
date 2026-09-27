package lre.constants;

import lre.annotation.RoboChartType;

/**
 * Safety thresholds of the AUV (LRE-DM4).
 */
public final class LreConstants {

    @RoboChartType("real")
    public static final double MIN_SAFE_DIST = 1.0;

    @RoboChartType("real")
    public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    @RoboChartType("real")
    public static final double STATIC_OBS_VERT_DIST = 1.0;

    @RoboChartType("real")
    public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    private LreConstants() {
    }
}
