package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.types.Angle;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Sensor surface of the Vehicle: the odometer reading and the pure functions over gas readings.
 * Every function returns a safe default for an empty reading.
 */
@SensorService
public final class VehicleSensors {

    private final Chem target;

    @RoboChartType("real")
    private double travelled;

    public VehicleSensors(Chem target) {
        this.target = target;
    }

    /** Records the cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /** gasD iff some value in the reading is for the target chemical with positive intensity. */
    public Status analysis(List<GasSensor> readings) {
        for (GasSensor sample : readings) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Maximum intensity across the reading; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        double peak = 0.0;
        boolean first = true;
        for (GasSensor sample : readings) {
            if (first || goreq(sample.i(), peak)) {
                peak = sample.i();
                first = false;
            }
        }
        return peak;
    }

    /** Direction of the first sensor carrying the peak intensity; Front for an empty reading. */
    public Angle location(List<GasSensor> readings) {
        double peak = intensity(readings);
        int position = 1;
        for (GasSensor sample : readings) {
            if (sample.i() == peak) {
                return angle(position);
            }
            position = position + 1;
        }
        return Angle.Front;
    }

    /** Sensing direction of the 1-based sensor position: 1 Front, 2 Right, 3 Back, 4 Left, repeating. */
    public Angle angle(@RoboChartType("nat") int position) {
        int slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Front;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }

    /** True iff intensity {@code first} is at least intensity {@code second}. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }
}
