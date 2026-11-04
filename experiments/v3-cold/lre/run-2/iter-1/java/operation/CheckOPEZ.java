package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is inside an Object Proximity Exclusion Zone
 * (LRE-OP2): the closest static obstacle is within MIN_SAFE_DIST, or the AUV
 * is at or above the surface.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez = false;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes inOpez. The closest static obstacle is the one CalcCStc
     * selects (sensor.closestStaticIndex()); with none, odist returns a safe
     * large distance.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in an OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
