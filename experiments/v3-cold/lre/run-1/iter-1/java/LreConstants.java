package lre;

import lre.annotation.RoboChartType;

/** Numeric values fixed by the LRE requirements. Distances in metres, velocities in m/s. */
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
    /** Velocity advised when control returns to the operator on endTask or once CAM is safe (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_ADVISED_VEL = 0.0;

    /** Highest AUV velocity at which MOM may be entered (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;
    /** Overall distance the closest obstacles must exceed for MOM to be entered (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;
    /** Horizontal velocity at or above which a nearby static obstacle forces HCM (LRE-Beh9). */
    @RoboChartType("real") public static final double HORIZ_VEL_LIMIT = 1.0;
    /** Vertical velocity at or above which a nearby static obstacle forces HCM (LRE-Beh11). */
    @RoboChartType("real") public static final double VERT_VEL_LIMIT = 1.0;
    /** Depth at or below which the AUV is in the exclusion zone (LRE-OP2). */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;
    /** Added to the squared relative speed (m^2/s^2) so the CPA time is defined when the relative speed is zero (LRE-OP5). */
    @RoboChartType("real") public static final double CPA_SPEED_EPSILON = 0.000001;

    private LreConstants() {}
}
