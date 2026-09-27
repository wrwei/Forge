package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Pure functions over a multi-sensor gas reading. Position {@code x} (1-based) in a
 * reading is the sensor facing {@code Angle.values()[(x - 1) % 4]}. Every function
 * returns a safe default for an empty reading.
 */
@SensorService
public final class GasSensorFunctions {

    private final Chem target;

    public GasSensorFunctions(Chem target) {
        this.target = target;
    }

    /** {@code gasD} iff some sample reports the target chemical with positive intensity. */
    public Status analysis(List<GasSample> readings) {
        for (var sample : readings) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity of the reading; {@code 0.0} for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (var sample : readings) {
            if (!goreq(peak, sample.i())) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /** Direction of the sensor with the peak intensity; {@code Front} for an empty reading. */
    public Angle location(List<GasSample> readings) {
        var peak = intensity(readings);
        for (var x = 1; x <= readings.size(); x++) {
            if (goreq(readings.get(x - 1).i(), peak)) {
                return angle(x);
            }
        }
        return Angle.Front;
    }

    /** True iff {@code lhs} is at least as large as {@code rhs}. */
    public boolean goreq(@RoboChartType("real") double lhs, @RoboChartType("real") double rhs) {
        return lhs >= rhs;
    }

    private static Angle angle(int position) {
        var directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
