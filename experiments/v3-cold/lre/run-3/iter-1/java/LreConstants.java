package lre;

import lre.annotation.RoboChartType;

/**
 * Safety thresholds and fixed values of the LRE. Distances are in metres,
 * speeds in m/s.
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle (m). */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity advised on entering MOM; also the highest AUV velocity at which MOM may be entered (m/s). */
    @RoboChartType("real") public static final double MOM_SPEED = 1.0;

    /** Velocity advised on entering HCM, on endTask in MOM and on leaving CAM once safe (m/s). */
    @RoboChartType("real") public static final double HALT_SPEED = 0.0;

    /** Horizontal or vertical AUV velocity at or above which a nearby static obstacle forces HCM (m/s). */
    @RoboChartType("real") public static final double APPROACH_SPEED = 1.0;

    /** Overall distance the closest static and dynamic obstacles must exceed for MOM to be entered (m). */
    @RoboChartType("real") public static final double MOM_ENTRY_CLEARANCE = 1.0;

    /** Added to the squared relative speed in CalcCPA so that tcpa stays finite at zero relative velocity (m^2/s^2). */
    @RoboChartType("real") public static final double MIN_REL_SPEED_SQ = 0.000001;

    private LreConstants() {}
}
