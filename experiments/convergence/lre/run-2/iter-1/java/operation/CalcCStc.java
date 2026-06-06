package lre.operation;

import lre.sensor.Sensor;

/**
 * Sets cstc to the index of the closest static obstacle (LRE-OP3);
 * -1 when no static obstacle exists. Invoked by the controller before
 * evaluating transition guards each step.
 */
public final class CalcCStc {

    private final Sensor sensor;

    private int cstc = -1;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    public int cstc() {
        return cstc;
    }
}
