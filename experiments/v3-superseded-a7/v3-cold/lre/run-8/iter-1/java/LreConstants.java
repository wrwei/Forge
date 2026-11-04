package lre;

import lre.annotation.RoboChartType;

/**
 * Fixed numeric values of the LRE: the safety thresholds of the AUV and the
 * limits and advised velocities the mode transitions use.
 */
public final class LreConstants {

    // LRE-DM4: safety thresholds (metres).

    /** Minimal safe distance to any obstacle (m). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    // LRE-Beh4: conditions for entering MOM from OCM.

    /** Highest AUV velocity at which MOM may be entered (m/s). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;

    /** Distance to the closest obstacles that must be exceeded to enter MOM (m). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    // LRE-Beh9, LRE-Beh11: velocities at which a nearby static obstacle calls for HCM.

    /** Horizontal velocity at or above which a near static obstacle triggers HCM (m/s). */
    @RoboChartType("real") public static final double HORIZ_CAUTION_VEL = 1.0;

    /** Vertical velocity at or above which a near static obstacle triggers HCM (m/s). */
    @RoboChartType("real") public static final double VERT_CAUTION_VEL = 1.0;

    // LRE-FR2, LRE-FR3, LRE-Beh7, LRE-Beh18: advised velocities.

    /** Velocity advised on entering MOM (m/s). */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;

    /** Velocity advised on entering HCM (m/s). */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;

    /** Velocity advised when handing control back to the operator after endTask or CAM (m/s). */
    @RoboChartType("real") public static final double STOP_ADVISED_VEL = 0.0;

    // LRE-OP5: keeps the CPA time finite when the closing velocity is zero.

    /** Added to the squared closing speed in the CPA time, so it is never divided by zero ((m/s)^2). */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {
    }
}
