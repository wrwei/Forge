package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import java.util.List;

/**
 * The gas-sensor array: classification functions over one multi-sensor reading.
 * Every function returns a safe default for an empty reading instead of failing.
 */
public final class GasSensorArray {

    /** analysis (CD-Fn1): {@code gasD} iff some sensor reports the target chemical, else {@code noGas}. */
    public Status analysis(List<GasSensor> readings) {
        for (var s : readings) {
            if (s.c() == Chem.TARGET && s.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** intensity (CD-Fn2): the maximum {@code i} across the reading; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var max = readings.get(0).i();
        for (var s : readings) {
            if (s.i() > max) {
                max = s.i();
            }
        }
        return max;
    }

    /**
     * location (CD-Fn3): the direction of the first sensor holding the highest intensity;
     * {@code Front} for an empty reading.
     */
    public Angle location(List<GasSensor> readings) {
        var best = 0;
        for (var x = 1; x < readings.size(); x++) {
            if (readings.get(x).i() > readings.get(best).i()) {
                best = x;
            }
        }
        return angle(best + 1);
    }

    /** goreq (CD-Fn4): true iff intensity {@code i1} is at least intensity {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    /** angle(x): maps a 1-based sensor position to a body direction, clockwise from Front. */
    Angle angle(@RoboChartType("nat") int x) {
        var slot = (x - 1) % 4;
        if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else if (slot == 3) {
            return Angle.Left;
        }
        return Angle.Front;
    }
}
