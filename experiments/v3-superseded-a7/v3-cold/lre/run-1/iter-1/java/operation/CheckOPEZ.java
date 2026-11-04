package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in the Object Proximity Exclusion Zone
 * (LRE-OP2; variable LRE-Var1).
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * inOpez holds when the closest static obstacle (the index CalcCStc reads
     * from the same sensor) is within MIN_SAFE_DIST, or the AUV is at or above
     * the surface. With no static obstacle the sensor returns a safe large
     * distance.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** Whether the AUV is in the OPEZ. */
    public boolean inOpez() {
        return inOpez;
    }
}
