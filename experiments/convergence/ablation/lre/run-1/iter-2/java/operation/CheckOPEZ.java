package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is within an Object Proximity Exclusion
 * Zone (LRE-OP2): inOpez is true when the overall distance to the
 * closest static obstacle (cstc, computed by CalcCStc) is at or below
 * minSafeDist, or the AUV depth is at or below zero.
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

    public boolean inOpez() {
        return inOpez;
    }
}
