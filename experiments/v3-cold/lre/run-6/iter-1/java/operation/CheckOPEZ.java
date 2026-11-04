package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is in an Object Proximity Exclusion Zone
 * (LRE-OP2): the closest static obstacle is within the minimal safe distance,
 * or the AUV is at or above the surface. With no static obstacle the sensor
 * reports a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez = false;

    /** Creates the operation over the AUV's sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the closest static obstacle (as CalcCStc selects it) and the depth. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** Whether the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
