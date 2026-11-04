package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Safety thresholds (LRE-DM4) and the fixed velocities the requirements give.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, metres. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, metres. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Normal operating speed in MOM, m/s (LRE-FR2); also the speed bound of LRE-Beh4, LRE-Beh9, LRE-Beh11. */
    @RoboChartType("real") public static final double NORMAL_SPEED = 1.0;

    /** Velocity advised on entering HCM, m/s (LRE-FR3). */
    @RoboChartType("real") public static final double CAUTION_SPEED = 0.0;

    /** Velocity advised when the LRE stops the AUV, m/s (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double HALT_SPEED = 0.0;

    /** Keeps the time to closest approach defined when the relative velocity is zero, (m/s)^2. */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
