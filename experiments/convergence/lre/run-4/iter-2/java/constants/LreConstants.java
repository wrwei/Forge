package lre.constants;

import lre.annotation.RoboChartType;

/**
 * AUV safety threshold constants (LRE-DM4).
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, metres. */
    @RoboChartType("real")
    public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, metres. */
    @RoboChartType("real")
    public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real")
    public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real")
    public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    private LreConstants() {
    }
}
