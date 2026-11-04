package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Decides whether the AUV is within an Object Proximity Exclusion Zone. */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * inOpez = odist(closest static obstacle) &lt;= minSafeDist, or depth &lt;= 0. The index is
     * the one CalcCStc stores, read from the same sensor function; with no static obstacle
     * odist is the sensor's safe large distance.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** True when the AUV is in an Object Proximity Exclusion Zone. */
    public boolean inOpez() {
        return inOpez;
    }
}
