package sranger.sensor;

import sranger.annotation.RoboChartType;

/** Exposes the latest forward IR distance reading. */
public final class Sensor {

    /** Reported when no reading is available; far above any obstacle threshold. */
    @RoboChartType("real")
    public static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /** Records a new IR measurement in metres; negative readings are clamped to zero. */
    public void update(@RoboChartType("real") double metres) {
        if (metres < 0.0) {
            this.latestDistance = 0.0;
        } else {
            this.latestDistance = metres;
        }
    }

    /** Latest IR distance in metres, or {@link #NO_READING_DISTANCE} before the first reading. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
