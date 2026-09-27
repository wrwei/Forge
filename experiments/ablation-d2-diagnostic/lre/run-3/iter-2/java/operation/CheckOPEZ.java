package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/** Determines whether the AUV is inside an Object Proximity Exclusion Zone. */
public final class CheckOPEZ {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.cstc) <= LreConstants.minSafeDist || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
