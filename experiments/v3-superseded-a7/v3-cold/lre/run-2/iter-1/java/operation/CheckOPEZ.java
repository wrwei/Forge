package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2): whether the AUV is in an Object Proximity Exclusion Zone.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** CheckOPEZ reading the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes inOpez: the closest static obstacle (the index CalcCStc selects, through the same
     * sensor function) is within minSafeDist, or the AUV is at or above the surface.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** True when the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
