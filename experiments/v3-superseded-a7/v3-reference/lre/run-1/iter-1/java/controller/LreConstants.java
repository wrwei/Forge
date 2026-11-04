package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements. Distances in metres, velocities
 * in m/s.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity advised on entering MOM (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;

    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;

    /** Velocity advised on leaving MOM by endTask and CAM when safe (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_ADVISED_VEL = 0.0;

    /** Highest AUV velocity at which OCM may hand over to MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;

    /** Distance to the closest obstacles that OCM to MOM must exceed (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_OBS_DIST = 1.0;

    /** Horizontal velocity at or above which MOM is cautious near a static obstacle (LRE-Beh9). */
    @RoboChartType("real") public static final double HCM_HORIZ_VEL_LIMIT = 1.0;

    /** Vertical velocity at or above which MOM is cautious near a static obstacle (LRE-Beh11). */
    @RoboChartType("real") public static final double HCM_VERT_VEL_LIMIT = 1.0;

    /** Depth at or below which the AUV is in the OPEZ (LRE-OP2). */
    @RoboChartType("real") public static final double OPEZ_SURFACE_DEPTH = 0.0;

    /**
     * Added to the squared closing speed in CalcCPA so that tcpa and cda stay
     * defined when the relative velocity is zero (not a requirement value).
     */
    @RoboChartType("real") public static final double CPA_SPEED_SQ_EPS = 0.000001;

    private LreConstants() {
    }
}
