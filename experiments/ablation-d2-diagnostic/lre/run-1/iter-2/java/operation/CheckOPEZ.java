package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone
 * (LRE-OP2). The no-static-obstacle case is absorbed by the Sensor, which
 * reports a safe large distance when no static obstacle exists.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    /** Recomputes inOpez for the current cycle. */
    public void compute() {
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.minSafeDist
                || sensor.depth() <= 0.0;
    }

    /** True when the AUV is inside an Object Proximity Exclusion Zone (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
