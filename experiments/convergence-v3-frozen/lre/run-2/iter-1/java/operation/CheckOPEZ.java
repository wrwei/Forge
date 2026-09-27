package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * LRE-OP2: decides whether the AUV is inside an Object Proximity Exclusion
 * Zone — either the closest static obstacle is within the minimal safe
 * distance, or the AUV has reached the surface. Invoked once per step before
 * the transition guards.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    public void compute() {
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** LRE-Var1: whether the AUV is currently in an OPEZ. */
    public boolean inOpez() {
        return inOpez;
    }
}
