package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.types.Angle;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Sensor-side functions over gas readings, plus the latest odometer sample.
 * Every function returns a safe default for an empty reading.
 */
@SensorService
public final class Sensor {

    private final Chem target;

    @RoboChartType("real")
    private double travelled;

    public Sensor(Chem target) {
        this.target = target;
    }

    /** Records the cumulative distance reported by the Vehicle's odometer. */
    public void recordOdometer(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /**
     * Classifies a reading: {@code gasD} when some sensor reports the target
     * chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> readings) {
        for (var reading : readings) {
            if (reading.c().equals(target) && reading.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity across the reading; {@code 0.0} for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (var reading : readings) {
            if (reading.i() > peak) {
                peak = reading.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor with the peak intensity (the first such sensor
     * on a tie); {@code Front} for an empty reading.
     */
    public Angle location(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        var best = 0;
        for (var x = 1; x < readings.size(); x++) {
            if (readings.get(x).i() > readings.get(best).i()) {
                best = x;
            }
        }
        return angle(best);
    }

    /** Sensing direction of the sensor at zero-based position {@code position}. */
    public Angle angle(@RoboChartType("nat") int position) {
        var directions = Angle.values();
        return directions[position % directions.length];
    }

    /** True iff intensity {@code i1} is at least intensity {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }
}
