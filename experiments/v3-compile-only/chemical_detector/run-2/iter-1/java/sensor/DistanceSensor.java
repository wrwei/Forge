package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.InputEvent;

/**
 * The Vehicle's odometer (CD-Evt3): keeps the cumulative distance reported by
 * the latest odometer event so the movement subsystem can sample it.
 */
public final class DistanceSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the distance carried by an odometer event. */
    public void update(InputEvent.Odometer reading) {
        this.distance = reading.distance();
    }

    /** The cumulative distance travelled, as last reported by the odometer. */
    @RoboChartType("real")
    public double distanceTravelled() {
        return distance;
    }
}
