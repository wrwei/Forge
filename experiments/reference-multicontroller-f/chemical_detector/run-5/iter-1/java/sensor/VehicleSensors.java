package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import java.util.List;

/**
 * Sensor surface of the Vehicle: the odometer sample and the functions that classify gas readings.
 * Every function returns a safe default for an empty reading.
 */
@SensorService
public final class VehicleSensors {

    private final Chem target;

    @RoboChartType("real")
    private double distanceTravelled;

    public VehicleSensors(Chem target) {
        this.target = target;
    }

    /** Records the latest cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distanceTravelled = distance;
    }

    /** Cumulative distance travelled, as last reported by the odometer. */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }

    /** analysis: gasD if some sensor reports the target chemical with positive intensity, noGas otherwise. */
    public Status analysis(List<GasSensor> readings) {
        for (GasSensor sensor : readings) {
            if (sensor.c().equals(target) && sensor.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** intensity: the peak intensity across the reading; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (GasSensor sensor : readings) {
            if (!goreq(peak, sensor.i())) {
                peak = sensor.i();
            }
        }
        return peak;
    }

    /** location: direction of the first sensor carrying the peak intensity; Front for an empty reading. */
    public Angle location(List<GasSensor> readings) {
        var peak = intensity(readings);
        for (int x = 1; x <= readings.size(); x++) {
            if (goreq(readings.get(x - 1).i(), peak)) {
                return angle(x);
            }
        }
        return Angle.Front;
    }

    /** goreq: true iff the first intensity is at least the second. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /** angle: sensing direction of the 1-based sensor position x, cycling Left, Right, Back, Front. */
    public Angle angle(@RoboChartType("nat") int x) {
        if (x < 1) {
            return Angle.Front;
        }
        var directions = Angle.values();
        return directions[(x - 1) % directions.length];
    }
}
