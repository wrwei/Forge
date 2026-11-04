package lre;

import lre.annotation.RoboChartType;

/** Safety thresholds and advised velocities of the LRE. */
public final class LreConstants {

    /** Minimal safe distance to any obstacle, metres. */
    @RoboChartType("real") public static final double MIN_SAFE_DIST = 1.0;

    /** Horizontal distance threshold to a static obstacle, metres. */
    @RoboChartType("real") public static final double STATIC_OBS_HORIZ_DIST = 1.0;

    /** Vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real") public static final double STATIC_OBS_VERT_DIST = 1.0;

    /** Default vertical distance threshold to a static obstacle, metres. */
    @RoboChartType("real") public static final double STATIC_OBS_DFLT_VERT_DIST = 1.0;

    /** Velocity threshold of the OCM-to-MOM and MOM-to-HCM guards, m/s. */
    @RoboChartType("real") public static final double VEL_LIMIT = 1.0;

    /** Velocity advised on entering MOM, m/s. */
    @RoboChartType("real") public static final double MOM_VELOCITY = 1.0;

    /** Velocity advised on entering HCM, m/s. */
    @RoboChartType("real") public static final double HCM_VELOCITY = 0.0;

    /** Velocity advised when the LRE returns to OCM after endTask or a cleared collision, m/s. */
    @RoboChartType("real") public static final double STOP_VELOCITY = 0.0;

    private LreConstants() {}
}
