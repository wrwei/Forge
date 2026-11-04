package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in an Object Proximity Exclusion Zone (LRE-OP2;
 * variable LRE-Var1).
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * inOpez = odist(cstc) &lt;= minSafeDist or depth &lt;= 0. The closest static
     * obstacle is the one CalcCStc selects, obtained from the same sensor function.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in the OPEZ. */
    public boolean inOpez() {
        return inOpez;
    }
}
