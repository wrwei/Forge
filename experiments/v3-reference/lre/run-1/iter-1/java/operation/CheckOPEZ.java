package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2): whether the AUV is in an Object Proximity Exclusion
 * Zone, i.e. the closest static obstacle is within the minimal safe distance
 * or the AUV is at or below depth zero. The closest static obstacle is the
 * one CalcCStc selects (the same sensor selection function, in the same step).
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** An operation reading the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the current readings. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** True when the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
