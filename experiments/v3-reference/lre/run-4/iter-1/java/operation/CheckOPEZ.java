package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone (LRE-OP2).
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * inOpez = odist(cstc) &lt;= minSafeDist or depth &lt;= 0, where cstc is the closest static
     * obstacle (the index CalcCStc selects, read from the same sensor function).
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** True when the AUV is in an OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
