package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in an Object Proximity Exclusion Zone: within
 * minSafeDist of the closest static obstacle, or at or above the surface.
 * The closest static obstacle is the one CalcCStc selects, read through the
 * same Sensor selection function.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Refreshes inOpez from the current sensor reading. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** True if the AUV is in the exclusion zone. */
    public boolean inOpez() {
        return inOpez;
    }
}
