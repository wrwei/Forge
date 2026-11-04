package lre;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the LRE requirements. Distances in metres,
 * velocities in m/s, times in seconds, depth in metres below the surface.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle (LRE-DM4: minSafeDist). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle (LRE-DM4: staticObsHorizDist). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle (LRE-DM4: staticObsVertDist). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle (LRE-DM4: staticObsDfltVertDist). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity advised on entering MOM (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_VEL = 1.0;

    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_VEL = 0.0;

    /** Velocity advised when stopping on a transition to OCM (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    /** Highest AUV velocity at which OCM may hand over to MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;

    /** Distance the closest obstacles must exceed for OCM to hand over to MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    /** Horizontal velocity at or above which a near static obstacle triggers HCM (LRE-Beh9). */
    @RoboChartType("real") public static final double HIGH_HORIZ_VEL = 1.0;

    /** Vertical velocity at or above which a near static obstacle triggers HCM (LRE-Beh11). */
    @RoboChartType("real") public static final double HIGH_VERT_VEL = 1.0;

    /** Depth at or below which the AUV is at the surface, hence in OPEZ (LRE-OP2). */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;

    /** Lowest time to closest point of approach that counts as a collision course (LRE-Beh8, LRE-Beh14). */
    @RoboChartType("real") public static final double MIN_TCPA = 0.0;

    private LreConstants() {
    }
}
