package lre;

import lre.annotation.RoboChartType;

/**
 * Numeric constants fixed by the LRE requirements. Distances are in metres,
 * velocities in m/s.
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
    @RoboChartType("real") public static final double MOM_VEL = 1.0;

    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_VEL = 0.0;

    /** Velocity advised when the LRE hands back to OCM on endTask or a cleared collision (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    /** Largest AUV velocity at which MOM may be entered (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;

    /** Overall distance the closest obstacles must exceed for MOM to be entered (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    /** Horizontal or vertical AUV velocity at or above which a nearby static obstacle calls for HCM (LRE-Beh9, LRE-Beh11). */
    @RoboChartType("real") public static final double CAUTION_VEL = 1.0;

    /** Added to the squared relative speed in CalcCPA so that the division is defined when there is no relative motion (LRE-OP5). */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {
    }
}
