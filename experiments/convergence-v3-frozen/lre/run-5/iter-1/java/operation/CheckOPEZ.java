package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/** Decides whether the AUV sits inside an Object Proximity Exclusion Zone. */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    public void compute() {
        this.inOpez = calcCStc.odistCstc() <= LreConstants.minSafeDist || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
