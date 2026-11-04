package lre;

import lre.annotation.RoboChartType;

/**
 * The numeric values the requirements fix. Distances in metres, velocities in
 * m/s.
 */
public final class LreConstants {

    /** minSafeDist: minimal safe distance to any obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** staticObsHorizDist: horizontal distance threshold to a static obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** staticObsVertDist: vertical distance threshold to a static obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** staticObsDfltVertDist: default vertical distance threshold to a static obstacle (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity advised on entering MOM (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_VELOCITY = 1.0;

    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_VELOCITY = 0.0;

    /** Velocity advised when a task ends or a collision threat clears (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VELOCITY = 0.0;

    /** The 1 m/s bound on vel, hvel and vvel in the mode-change guards (LRE-Beh4, LRE-Beh9, LRE-Beh11). */
    @RoboChartType("real") public static final double VEL_THRESHOLD = 1.0;

    /** The 1 m clearance to the closest obstacles required to enter MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double ENTRY_CLEARANCE_DIST = 1.0;

    /** Added to the squared relative speed so the CPA time is defined without relative motion (LRE-OP5). */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
