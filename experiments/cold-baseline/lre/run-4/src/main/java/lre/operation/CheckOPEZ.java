package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is in the Object Proximity Exclusion Zone.
 * See LRE-OP2.
 *
 * Reads cstc from CalcCStc rather than calling sensor.closestStaticIndex()
 * directly, making the dependency explicit. The sentinel case is handled by
 * the Sensor layer (returns Double.MAX_VALUE when cstc == -1).
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
