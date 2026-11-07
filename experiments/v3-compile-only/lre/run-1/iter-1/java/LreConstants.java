package lre;

import lre.annotation.RoboChartType;

/**
 * Numeric values fixed by the LRE requirements (LRE-DM4 and the transition
 * requirements). Distances in metres, velocities in m/s, times in seconds.
 */
public final class LreConstants {

    /** LRE-DM4: minimal safe distance to any obstacle (m). */
    @RoboChartType("real") public static final double minSafeDist = 1.0;

    /** LRE-DM4: horizontal distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double staticObsHorizDist = 1.0;

    /** LRE-DM4: vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double staticObsVertDist = 1.0;

    /** LRE-DM4: default vertical distance threshold to a static obstacle (m). */
    @RoboChartType("real") public static final double staticObsDfltVertDist = 1.0;

    /** LRE-FR2: velocity advised on entering MOM (m/s). */
    @RoboChartType("real") public static final double momVelocity = 1.0;

    /** LRE-FR3: velocity advised on entering HCM (m/s). */
    @RoboChartType("real") public static final double hcmVelocity = 0.0;

    /** LRE-Beh7, LRE-Beh18: velocity advised when the LRE hands back to OCM (m/s). */
    @RoboChartType("real") public static final double stopVelocity = 0.0;

    /** LRE-Beh4: maximum AUV velocity for entering MOM (m/s). */
    @RoboChartType("real") public static final double momEntryMaxVel = 1.0;

    /** LRE-Beh4: overall distance to the closest obstacles must exceed this to enter MOM (m). */
    @RoboChartType("real") public static final double momEntryMinDist = 1.0;

    /** LRE-Beh9, LRE-Beh11: velocity at or above which a static obstacle forces HCM (m/s). */
    @RoboChartType("real") public static final double highVel = 1.0;

    /** LRE-OP2: depth at or below which the AUV is in the OPEZ (m below surface). */
    @RoboChartType("real") public static final double surfaceDepth = 0.0;

    /** LRE-Beh8, LRE-Beh14: smallest time to closest point of approach that is a risk (s). */
    @RoboChartType("real") public static final double minTcpa = 0.0;

    /** LRE-OP5: added to the squared closing speed so the CPA division is always defined (m^2/s^2). */
    @RoboChartType("real") public static final double cpaEpsilon = 0.000001;

    private LreConstants() {
    }
}
