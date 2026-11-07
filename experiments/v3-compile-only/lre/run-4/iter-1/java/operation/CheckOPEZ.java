package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in an Object Proximity Exclusion Zone (LRE-OP2):
 * the overall distance to the closest static obstacle is at or below
 * minSafeDist, or the AUV is at or above the surface.
 *
 * <p>The closest static obstacle is the one CalcCStc selects: both read
 * {@code sensor.closestStaticIndex()} on the same sensor state. When there is
 * no static obstacle the sensor reports a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez = false;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the current sensor readings. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in an Object Proximity Exclusion Zone (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
