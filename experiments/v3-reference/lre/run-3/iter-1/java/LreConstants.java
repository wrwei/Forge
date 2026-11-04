package lre;

import lre.annotation.RoboChartType;

/**
 * Fixed numeric values of the LRE: the four safety thresholds of the AUV and
 * the other values the requirements fix.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle (metres). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle (metres). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle (metres). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle (metres). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity advised on entering MOM (m/s). */
    @RoboChartType("real") public static final double MOM_ADVISED_VEL = 1.0;

    /** Velocity advised on entering HCM (m/s). */
    @RoboChartType("real") public static final double HCM_ADVISED_VEL = 0.0;

    /** Velocity advised when the LRE stops the AUV on returning to OCM (m/s). */
    @RoboChartType("real") public static final double STOP_VEL = 0.0;

    /** Highest AUV velocity at which MOM may be entered (m/s). */
    @RoboChartType("real") public static final double MOM_ENTRY_MAX_VEL = 1.0;

    /** Overall distance to the closest obstacles that MOM entry requires to be exceeded (metres). */
    @RoboChartType("real") public static final double MOM_ENTRY_MIN_DIST = 1.0;

    /** Horizontal or vertical velocity at or above which a nearby static obstacle calls for HCM (m/s). */
    @RoboChartType("real") public static final double CAUTION_VEL = 1.0;

    /** Regularisation term of the CPA time denominator ((m/s)^2), so it is never zero. */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {
    }
}
