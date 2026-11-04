package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Whether the AUV is in an Object Proximity Exclusion Zone (LRE-OP2): the
 * closest static obstacle is within MIN_SAFE_DIST, or the AUV is at or above
 * the surface. A missing static obstacle reads as a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    // The same closest-static-obstacle index CalcCStc reads in this step.
    @RoboChartType("nat") private int staticIndex;
    private boolean inOpez;

    /** An operation reading {@code sensor}. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the sensor. */
    public void compute() {
        this.staticIndex = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.staticIndex) <= LreConstants.MIN_SAFE_DIST || sensor.depth() <= 0.0;
    }

    /** True when the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() { return inOpez; }
}
