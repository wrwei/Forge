package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/** CheckOPEZ: whether the AUV is inside an Object Proximity Exclusion Zone. */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes inOpez: the closest static obstacle (the index CalcCStc selects) is within
     * MIN_SAFE_DIST, or the AUV is at or above the surface.
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
