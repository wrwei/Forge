package lre;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the requirements. Distances in metres, velocities
 * in m/s.
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

    /** Velocity limit used by LRE-Beh4 (vel), LRE-Beh9 (hvel) and LRE-Beh11 (vvel). */
    @RoboChartType("real") public static final double VEL_LIMIT = 1.0;

    /** Overall distance to the closest obstacles that must be exceeded to enter MOM (LRE-Beh4). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    /** Velocity advised on entering MOM (LRE-FR2). */
    @RoboChartType("real") public static final double MOM_VELOCITY = 1.0;

    /** Velocity advised on entering HCM (LRE-FR3). */
    @RoboChartType("real") public static final double HCM_VELOCITY = 0.0;

    /** Velocity advised when the LRE hands control back to the operator (LRE-Beh7, LRE-Beh18). */
    @RoboChartType("real") public static final double STOP_VELOCITY = 0.0;

    /**
     * Added to the squared relative speed in CalcCPA so that its division is
     * always defined (m^2/s^2).
     */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000000001;

    private LreConstants() {}
}
