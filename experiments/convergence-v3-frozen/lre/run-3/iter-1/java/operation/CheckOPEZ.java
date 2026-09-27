package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/** Decides whether the AUV is inside an Object Proximity Exclusion Zone. */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    /** Recomputes the exclusion-zone status from the current sensor reading. */
    public void compute() {
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is inside an Object Proximity Exclusion Zone. */
    public boolean inOpez() {
        return inOpez;
    }
}
