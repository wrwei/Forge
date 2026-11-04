package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Safety thresholds of the AUV (LRE-DM4) and the other fixed values the LRE
 * requirements state.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity advised on entering MOM, m/s (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_VELOCITY = 1.0;

    /** Velocity advised on entering HCM, m/s (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_VELOCITY = 0.0;

    /** Velocity advised on MOM to OCM by endTask and on CAM to OCM when safe, m/s (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VELOCITY = 0.0;

    /** Largest AUV velocity at which OCM may hand over to MOM, m/s (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;

    /** Overall distance to the closest obstacles that OCM to MOM must exceed, metres (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    /** Horizontal velocity at or above which a near static obstacle forces HCM, m/s (LRE-Beh9). */
    @RoboChartType("real") public static final double HORIZ_VEL_LIMIT = 1.0;

    /** Vertical velocity at or above which a near static obstacle forces HCM, m/s (LRE-Beh11). */
    @RoboChartType("real") public static final double VERT_VEL_LIMIT = 1.0;

    /** Depth at or below which the AUV is in the OPEZ, metres (LRE-OP2). */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;

    /** Smallest time to closest point of approach of a collision course, seconds (LRE-Beh8, LRE-Beh14). */
    @RoboChartType("real") public static final double MIN_TCPA = 0.0;

    /**
     * Added to the squared relative speed in CalcCPA so that tcpa stays defined
     * when the relative velocity is zero, m^2/s^2 (LRE-OP5; not given by the
     * requirements).
     */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {
    }
}
