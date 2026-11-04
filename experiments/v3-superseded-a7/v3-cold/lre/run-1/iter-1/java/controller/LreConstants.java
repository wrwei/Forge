package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements, with their units.
 */
public final class LreConstants {

    // --- Safety thresholds of the AUV (LRE-DM4), metres ---

    /** Minimal safe distance to any obstacle (m). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;
    /** Horizontal distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;
    /** Vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;
    /** Default vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    // --- Advised velocities (m/s) ---

    /** Velocity advised on entering MOM (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_VEL = 1.0;
    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_VEL = 0.0;
    /** Velocity advised when the LRE hands back to OCM on endTask or after CAM (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    // --- Transition thresholds fixed by the behavioural requirements ---

    /** Highest AUV velocity (m/s) at which OCM may hand over to MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;
    /** Overall distance (m) that the closest obstacles must exceed for OCM to hand over to MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;
    /** Horizontal velocity (m/s) at or above which a near static obstacle triggers HCM (LRE-Beh9). */
    @RoboChartType("real") public static final double HIGH_HORIZ_VEL = 1.0;
    /** Vertical velocity (m/s) at or above which a near static obstacle triggers HCM (LRE-Beh11). */
    @RoboChartType("real") public static final double HIGH_VERT_VEL = 1.0;
    /** Least time to closest point of approach (s) at which the approach still lies ahead (LRE-Beh8, LRE-Beh14). */
    @RoboChartType("real") public static final double MIN_TCPA = 0.0;
    /** Depth (m below surface) at or below which the AUV is in the OPEZ (LRE-OP2). */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;

    // --- Sensor-layer conventions ---

    /** Obstacle index meaning "no such obstacle"; registered obstacles have indices from 1 (LRE-DM5, LRE-Var5, LRE-Var6). */
    @RoboChartType("nat") public static final int NO_OBSTACLE = 0;
    /** Safe large distance (m) returned by the distance functions when no obstacle exists (LRE-DM5). */
    @RoboChartType("real") public static final double NO_OBSTACLE_DIST = 1000000.0;
    /**
     * Small positive term (m/s)^2 that keeps the CPA denominator non-zero (LRE-OP5).
     * It inflates cda^2 by about CPA_EPSILON * hdist^2 / |closing velocity|^2.
     */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000000000001;

    private LreConstants() {}
}
