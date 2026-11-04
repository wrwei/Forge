package lre.controller;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, metres (LRE-DM4). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity threshold of 1 m/s used by LRE-Beh4, LRE-Beh9 and LRE-Beh11. */
    @RoboChartType("real") public static final double VEL_LIMIT = 1.0;

    /** Distance to the closest obstacles that MOM entry requires to be exceeded, metres (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_CLEARANCE = 1.0;

    /** Velocity advised on entering MOM, m/s (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;

    /** Velocity advised on entering HCM, m/s (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;

    /** Velocity advised when leaving MOM on endTask or CAM once safe, m/s (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    /** Depth at or below which the AUV is in the OPEZ, metres (LRE-OP2). */
    @RoboChartType("real") public static final double SURFACE_DEPTH = 0.0;

    private LreConstants() {}
}
