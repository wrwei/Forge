package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements.
 */
public final class LreConstants {

    // LRE-DM4: safety thresholds, metres.
    /** Minimal safe distance to any obstacle, m. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;
    /** Horizontal distance threshold to a static obstacle, m. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;
    /** Vertical distance threshold to a static obstacle, m. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;
    /** Default vertical distance threshold to a static obstacle, m. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    // LRE-FR2, LRE-FR3, LRE-Beh7, LRE-Beh18: advised velocities, m/s.
    /** Velocity advised on entering MOM, m/s. */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;
    /** Velocity advised on entering HCM, m/s. */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;
    /** Velocity advised when MOM ends its task or CAM hands back to OCM, m/s. */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    // LRE-Beh4: conditions for engaging MOM from OCM.
    /** Highest AUV velocity at which MOM may be engaged, m/s. */
    @RoboChartType("real") public static final double ENGAGE_MAX_VEL = 1.0;
    /** Distance to the closest obstacles that must be exceeded to engage MOM, m. */
    @RoboChartType("real") public static final double ENGAGE_MIN_DIST = 1.0;

    // LRE-Beh9, LRE-Beh11: velocity thresholds for entering HCM, m/s.
    /** Horizontal velocity at or above which a near static obstacle forces HCM, m/s. */
    @RoboChartType("real") public static final double HIGH_HORIZ_VEL = 1.0;
    /** Vertical velocity at or above which a near static obstacle forces HCM, m/s. */
    @RoboChartType("real") public static final double HIGH_VERT_VEL = 1.0;

    // LRE-OP5: keeps the tcpa denominator positive when the relative velocity is zero, (m/s)^2.
    /** Regularisation added to the squared relative speed in CalcCPA, (m/s)^2. */
    @RoboChartType("real") public static final double CPA_SPEED_EPS = 0.000000001;

    private LreConstants() {}
}
