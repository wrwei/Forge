package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2). Sets inOpez to true when the overall distance to the closest
 * static obstacle (cstc) is at or below minSafeDist, or the AUV depth is at or below zero.
 * Reads cstc from CalcCStc to make the dependency explicit.
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
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.minSafeDist || sensor.depth() <= 0.0;
    }

    public boolean inOpez() { return inOpez; }
}
