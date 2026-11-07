package lre.controller;

import lre.annotation.RoboChartType;

/**
 * The safety thresholds of the AUV and the other fixed values the LRE uses.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, metres. */
    @RoboChartType("real")
    public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, metres. */
    @RoboChartType("real")
    public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real")
    public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real")
    public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /**
     * Normal operating speed, m/s: the velocity advised on entering MOM, and
     * the velocity bound of the OCM-to-MOM and MOM-to-HCM guards.
     */
    @RoboChartType("real")
    public static final double MOM_VEL = 1.0;

    /** Advised velocity that brings the AUV to a halt, m/s. */
    @RoboChartType("real")
    public static final double STOP_VEL = 0.0;

    /** Depth of the water surface, metres below surface. */
    @RoboChartType("real")
    public static final double SURFACE_DEPTH = 0.0;

    /** Smallest time to the closest point of approach that is not in the past, seconds. */
    @RoboChartType("real")
    public static final double MIN_TCPA = 0.0;

    /**
     * Added to the squared relative speed in the CPA computation so that tcpa
     * is defined when the relative velocity is zero, m^2/s^2.
     */
    @RoboChartType("real")
    public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {
    }
}
