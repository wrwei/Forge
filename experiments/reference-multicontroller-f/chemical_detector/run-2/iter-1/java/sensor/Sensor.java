package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.types.Angle;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Vehicle sensor surface (odometer) and the gas-analysis functions over a reading.
 * Every function returns a safe default for an empty reading.
 */
@SensorService
public final class Sensor {

    @RoboChartType("real")
    private double distance;

    /** Records the cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /** gasD if any sensor in the reading reports the target chemical, noGas otherwise. */
    public Status analysis(List<GasSensor> readings) {
        for (var reading : readings) {
            if (reading.c() == Chem.target) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity across the reading; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (var reading : readings) {
            if (!goreq(peak, reading.i())) {
                peak = reading.i();
            }
        }
        return peak;
    }

    /** Direction of the sensor with the peak intensity; Front for an empty reading. */
    public Angle location(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        var strongest = 0;
        for (var x = 1; x < readings.size(); x++) {
            if (!goreq(readings.get(strongest).i(), readings.get(x).i())) {
                strongest = x;
            }
        }
        return angle(strongest + 1);
    }

    /** True iff intensity {@code first} is at least {@code second}. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private Angle angle(@RoboChartType("nat") int position) {
        var directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
