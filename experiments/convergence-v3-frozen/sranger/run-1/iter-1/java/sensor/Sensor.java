package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The SRanger sensor layer (SR-DM5). It exposes the single measurement the
 * controller consumes: the latest forward-facing IR distance reading.
 */
public final class Sensor {

    /**
     * Latest IR distance reading in metres. Initialised to a large value so
     * that the obstacle-detection condition is never satisfied by missing
     * data (SR-DM5).
     */
    @RoboChartType("real")
    private double distance = 1000.0;

    /** Records a fresh IR measurement, in metres. Negative readings are clamped to zero. */
    public void update(@RoboChartType("real") double reading) {
        if (reading < 0.0) {
            this.distance = 0.0;
        } else {
            this.distance = reading;
        }
    }

    /** Latest IR distance reading in metres, non-negative (SR-SF1). */
    @RoboChartType("real")
    public double distance() {
        return distance;
    }
}
