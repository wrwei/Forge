package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Whether the AUV is in the Object Proximity Exclusion Zone (LRE-OP2). The
 * closest static obstacle is the one CalcCStc selects, read through the same
 * sensor query; with no static obstacle the sensor's large default distance
 * applies.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** An operation reading depth and obstacle distances from {@code sensor}. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the sensor. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.OPEZ_SURFACE_DEPTH;
    }

    /** True if the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
