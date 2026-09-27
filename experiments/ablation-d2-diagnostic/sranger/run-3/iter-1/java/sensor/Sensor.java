package sranger.sensor;

import sranger.annotation.RoboChartType;
import sranger.constants.SRangerConstants;

/**
 * Exposes the latest forward IR distance reading to the controller.
 */
public final class Sensor {

    @RoboChartType("real")
    private double latestDistance = SRangerConstants.NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres; negative readings are clamped to zero.
     */
    public void update(@RoboChartType("real") double measuredDistance) {
        if (measuredDistance < 0.0) {
            this.latestDistance = 0.0;
        } else {
            this.latestDistance = measuredDistance;
        }
    }

    /**
     * Returns the latest IR distance in metres, or a large default when no reading exists.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
