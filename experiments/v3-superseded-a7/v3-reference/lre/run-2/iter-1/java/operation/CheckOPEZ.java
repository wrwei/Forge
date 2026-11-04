package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CheckOPEZ (LRE-OP2): whether the AUV is in an Object Proximity Exclusion Zone,
 * that is, within minSafeDist of the closest static obstacle or at or above
 * the surface. With no static obstacle the sensor reports a safe large
 * distance, so only the depth condition can hold.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    // The closest static obstacle, read from the sensor as CalcCStc reads it.
    @RoboChartType("nat") private int staticIndex;
    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the sensor. */
    public void compute() {
        this.staticIndex = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.staticIndex) <= LreConstants.MIN_SAFE_DIST || sensor.depth() <= 0.0;
    }

    /** True when the AUV is in the exclusion zone (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
