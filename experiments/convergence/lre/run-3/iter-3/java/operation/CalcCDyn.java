package lre.operation;

import lre.sensor.Sensor;

/**
 * Sets cdyn to the index of the closest dynamic obstacle (LRE-OP4).
 * Value -1 means no dynamic obstacle exists.
 */
public final class CalcCDyn {

    private final Sensor sensor;
    private int cdyn = -1;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    public int cdyn() {
        return cdyn;
    }
}
