package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Sensor surface of the Vehicle: the odometer and the functions that classify gas readings.
 * Each function returns a safe default for an empty reading.
 */
@SensorService
public final class ChemSensors {

    @RoboChartType("real")
    private double travelled;

    /** Records the cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /** gasD if some value in the reading is the target chemical at positive intensity, noGas otherwise. */
    public Status analysis(List<GasSensor> readings) {
        for (var reading : readings) {
            if (reading.c() == ChemConstants.TARGET_CHEM && reading.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Maximum intensity across the reading; 0 for an empty reading. */
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

    /** Direction of the first sensor whose intensity is the peak; Front for an empty reading. */
    public Angle location(List<GasSensor> readings) {
        var peak = intensity(readings);
        for (int index = 0; index < readings.size(); index++) {
            if (goreq(readings.get(index).i(), peak)) {
                return angle(index + 1);
            }
        }
        return Angle.Front;
    }

    /** True iff {@code first} is at least as large as {@code second}. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private Angle angle(@RoboChartType("nat") int position) {
        var direction = (position - 1) % 4;
        if (direction == 0) {
            return Angle.Front;
        } else if (direction == 1) {
            return Angle.Right;
        } else if (direction == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }
}
