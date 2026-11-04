package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is in an Object Proximity Exclusion Zone: the
 * closest static obstacle is within the minimal safe distance, or the AUV is
 * at or above the surface.
 */
public final class CheckOPEZ {
    private final Sensor sensor;
    @RoboChartType("nat") private int staticIndex;
    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) { this.sensor = sensor; }

    public void compute() {
        this.staticIndex = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.staticIndex) <= LreConstants.MIN_SAFE_DIST || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in the OPEZ. */
    public boolean inOpez() { return inOpez; }
}
