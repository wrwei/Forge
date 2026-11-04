package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2): whether the AUV is in an Object Proximity Exclusion
 * Zone. The closest static obstacle is the one CalcCStc selects,
 * {@code sensor.closestStaticIndex()}; with no static obstacle the sensor
 * reports a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez = false;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the current sensor reading. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** Whether the AUV is in an OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
