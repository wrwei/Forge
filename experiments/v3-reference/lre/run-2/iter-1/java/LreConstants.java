package lre;

import lre.annotation.RoboChartType;

/**
 * Constants of the LRE: the four safety thresholds of LRE-DM4 (metres) and
 * the advised speeds of LRE-FR2, LRE-FR3, LRE-Beh7 and LRE-Beh18 (m/s).
 */
public final class LreConstants {

    /** Minimal safe distance to any obstacle. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Normal operating speed: advised on entering MOM, and the 1 m/s limit in the guards. */
    @RoboChartType("real") public static final double NORMAL_SPEED = 1.0;

    /** Speed advised on entering HCM. */
    @RoboChartType("real") public static final double HCM_SPEED = 0.0;

    /** Speed advised when the LRE hands control back after endTask or a cleared collision. */
    @RoboChartType("real") public static final double STOP_SPEED = 0.0;

    /** Added to the squared closing speed in CalcCPA so the division is never by zero. */
    @RoboChartType("real") public static final double CPA_EPSILON = 0.000001;

    private LreConstants() {}
}
