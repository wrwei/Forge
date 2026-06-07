package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is within an Object Proximity Exclusion
 * Zone: inOpez is true when the overall distance to the closest static
 * obstacle is at or below minSafeDist, or the AUV depth is at or below
 * zero. Invoked by the controller before evaluating transition guards
 * each step.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstcIdx = -1;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstcIdx = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.cstcIdx) <= LreConstants.minSafeDist
                || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
