package sranger.sensor;

import sranger.annotation.RoboChartType;
import sranger.annotation.SensorService;

/**
 * SR-DM5 / SR-SF1: exposes the latest IR distance reading.
 * Returns a large default value when uninitialised so the obstacle
 * condition is not falsely triggered.
 */
@SensorService
public final class Sensor {

    @RoboChartType("real")
    private static final double DEFAULT_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double latestDistance;

    private boolean initialised;

    public Sensor() {
        this.latestDistance = DEFAULT_DISTANCE;
        this.initialised = false;
    }

    /** Push a new IR distance reading (metres, non-negative). */
    public void update(@RoboChartType("real") double distance) {
        this.latestDistance = distance;
        this.initialised = true;
    }

    /** Reset the sensor to its uninitialised state. */
    public void reset() {
        this.latestDistance = DEFAULT_DISTANCE;
        this.initialised = false;
    }

    /** SR-SF1: latest IR distance, metres. Large default when uninitialised. */
    @RoboChartType("real")
    public double distance() {
        if (this.initialised) {
            return this.latestDistance;
        } else {
            return DEFAULT_DISTANCE;
        }
    }
}
