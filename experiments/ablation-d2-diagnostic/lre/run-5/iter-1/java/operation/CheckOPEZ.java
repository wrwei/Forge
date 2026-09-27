package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone: the
 * closest static obstacle is within the minimal safe distance, or the AUV has
 * reached the surface.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private final CalcCStc calcCStc;

    @RoboChartType("nat")
    private int closestStatic = -1;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    public void compute() {
        this.closestStatic = calcCStc.cstc();
        this.inOpez = sensor.odist(this.closestStatic) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
