package lre.operation;

import lre.sensor.Sensor;

/**
 * Sets cdyn to the index of the closest dynamic obstacle, or -1 if no
 * dynamic obstacle exists (LRE-OP4).
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
