package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements.
 */
public final class LreConstants {

    // LRE-DM4: safety thresholds, in metres.

    /** Minimal safe distance to any obstacle. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;
    /** Horizontal distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;
    /** Vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;
    /** Default vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    // LRE-FR2, LRE-FR3, LRE-Beh7, LRE-Beh18: advised velocities, in m/s.

    /** Velocity advised on entering MOM. */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;
    /** Velocity advised on entering HCM. */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;
    /** Velocity advised when leaving MOM on endTask or leaving CAM once safe. */
    @RoboChartType("real") public static final double STOP_ADVISED_VEL = 0.0;

    // LRE-Beh4: conditions for entering MOM.

    /** Highest AUV velocity (m/s) at which MOM may be entered. */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;
    /** Overall distance (m) to the closest obstacles that must be exceeded to enter MOM. */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    // LRE-Beh9, LRE-Beh11: velocity limits near a static obstacle, in m/s.

    /** Horizontal velocity at or above which a near static obstacle forces HCM. */
    @RoboChartType("real") public static final double HORIZ_VEL_LIMIT = 1.0;
    /** Vertical velocity at or above which a near static obstacle forces HCM. */
    @RoboChartType("real") public static final double VERT_VEL_LIMIT = 1.0;

    // LRE-OP2, LRE-Beh8, LRE-Beh14: reference values.

    /** Depth (m) at or below which the AUV is in the OPEZ. */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;
    /** Lowest time (s) to the closest point of approach that counts as a collision course. */
    @RoboChartType("real") public static final double MIN_TCPA = 0.0;

    // LRE-OP5: not given by the requirements (see post_codegen.md).

    /** Added to the squared relative speed ((m/s)^2) so the CPA division is never by zero. */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
