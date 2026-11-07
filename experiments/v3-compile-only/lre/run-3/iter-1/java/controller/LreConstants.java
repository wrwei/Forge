package lre.controller;

import lre.annotation.RoboChartType;

/**
 * The AUV's safety thresholds (LRE-DM4), in metres.
 */
public final class LreConstants {
    /** Minimal safe distance to any obstacle (m). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;
    /** Horizontal distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;
    /** Vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;
    /** Default vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    private LreConstants() {
    }
}
