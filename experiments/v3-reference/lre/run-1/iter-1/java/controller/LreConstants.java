package lre.controller;

import lre.annotation.RoboChartType;

/**
 * The LRE's fixed thresholds and advised values. Distances in metres,
 * velocities in m/s, times in seconds.
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

    /** Velocity limit for entering MOM and for the velocity-based HCM transitions (LRE-Beh4, Beh9, Beh11). */
    @RoboChartType("real") public static final double VEL_LIMIT = 1.0;

    /** Clearance both closest obstacles must exceed for MOM to be entered (LRE-Beh4). */
    @RoboChartType("real") public static final double OBS_CLEARANCE = 1.0;

    /** Velocity advised on entering MOM (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;

    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;

    /** Velocity advised when a task ends or a collision threat has passed (LRE-Beh7, Beh18). */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    /** Depth at or below which the AUV is in the OPEZ (LRE-OP2). */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;

    /** Least time to closest point of approach that still counts as a threat (LRE-Beh8, Beh14). */
    @RoboChartType("real") public static final double MIN_TCPA = 0.0;

    /** Added to the squared relative speed so the CPA division is always defined (LRE-OP5). */
    @RoboChartType("real") public static final double MIN_REL_SPEED_SQ = 0.000001;

    private LreConstants() {
    }
}
