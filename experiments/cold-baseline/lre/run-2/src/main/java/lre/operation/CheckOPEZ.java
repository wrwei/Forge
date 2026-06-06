package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2) — determines whether the AUV is within an Object
 * Proximity Exclusion Zone.
 * <p>
 * Reads {@code cstc} from {@link CalcCStc} (not from the sensor directly)
 * so the dependency on CalcCStc is explicit. The sentinel case
 * (cstc == -1) is handled by the Sensor layer, which returns
 * Double.MAX_VALUE for odist when no static obstacle exists.
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

    public boolean inOpez() {
        return inOpez;
    }
}
