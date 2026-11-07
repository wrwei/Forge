package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2): whether the AUV is in an Object Proximity Exclusion
 * Zone (LRE-Var1): the overall distance to the closest static obstacle is at
 * or below minSafeDist, or the depth is at or below zero. The closest static
 * obstacle is the one CalcCStc selects, sensor.closestStaticIndex(); with no
 * static obstacle the sensor returns a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the current sensor readings. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in the Object Proximity Exclusion Zone. */
    public boolean inOpez() {
        return inOpez;
    }
}
