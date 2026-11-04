package lre;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements. Distances in metres, velocities in m/s.
 */
public final class LreConstants {

    // LRE-DM4: safety thresholds.
    /** Minimal safe distance to any obstacle. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;
    /** Horizontal distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;
    /** Vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;
    /** Default vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    // LRE-FR2, LRE-FR3, LRE-Beh7, LRE-Beh18: advised velocities.
    /** Velocity advised on entering MOM. */
    @RoboChartType("real") public static final double MOM_ADV_VEL = 1.0;
    /** Velocity advised on entering HCM. */
    @RoboChartType("real") public static final double HCM_ADV_VEL = 0.0;
    /** Velocity advised when MOM ends its task or CAM hands back to the operator. */
    @RoboChartType("real") public static final double STOP_ADV_VEL = 0.0;

    // LRE-Beh4: conditions for entering MOM.
    /** Highest AUV velocity at which MOM may be entered. */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;
    /** Overall distance to the closest obstacles that must be exceeded to enter MOM. */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_OBS_DIST = 1.0;

    // LRE-Beh9, LRE-Beh11: velocities at which a nearby static obstacle calls for HCM.
    /** Horizontal velocity at or above which a close static obstacle triggers HCM. */
    @RoboChartType("real") public static final double HIGH_HORIZ_VEL = 1.0;
    /** Vertical velocity at or above which a close static obstacle triggers HCM. */
    @RoboChartType("real") public static final double HIGH_VERT_VEL = 1.0;

    // LRE-OP5: not given by the requirements; keeps the CPA division defined
    // when the AUV and the obstacle have the same velocity.
    /** Added to the squared closing speed in the CPA computation, (m/s)^2. */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
