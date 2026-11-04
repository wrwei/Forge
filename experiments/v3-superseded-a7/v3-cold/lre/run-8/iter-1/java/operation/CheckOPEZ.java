package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is in an Object Proximity Exclusion Zone.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * inOpez = odist(cstc) <= minSafeDist or depth <= 0. The closest static
     * obstacle is read from the same sensor function CalcCStc uses.
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
