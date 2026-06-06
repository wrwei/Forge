package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * LRE-OP2: Determines whether the AUV is in an Object Proximity Exclusion Zone.
 * Reads cstc from CalcCStc (explicit dependency).
 * Single boolean assignment — no conditional branching.
 * Sentinel case (cstc == -1) is handled by the Sensor layer.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
        this.inOpez = false;
    }

    public void compute() {
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.minSafeDist || sensor.depth() <= 0.0;
    }

    public boolean inOpez() { return inOpez; }
}
