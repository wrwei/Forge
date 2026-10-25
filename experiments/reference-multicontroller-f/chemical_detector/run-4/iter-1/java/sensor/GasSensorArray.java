package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Classification functions over a multi-directional gas-sensor reading.
 * Every function returns a safe default for an empty reading.
 */
public final class GasSensorArray {

    private final Chem target;

    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /** Returns gasD if some sensor reports the target chemical with positive intensity, else noGas. */
    public Status analysis(List<GasSensor> gs) {
        for (var sensor : gs) {
            if (sensor.c().equals(target) && sensor.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Returns the maximum intensity in the reading, or 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        var max = gs.get(0).i();
        for (var sensor : gs) {
            if (sensor.i() > max) {
                max = sensor.i();
            }
        }
        return max;
    }

    /** Returns the direction of the first sensor with the highest intensity, or Front for an empty reading. */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        var best = 0;
        for (var x = 1; x < gs.size(); x++) {
            if (gs.get(x).i() > gs.get(best).i()) {
                best = x;
            }
        }
        return angle(best + 1);
    }

    /** Returns true iff intensity {@code i1} is at least intensity {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    private static Angle angle(@RoboChartType("nat") int position) {
        var index = (position - 1) % 4;
        if (index == 0) {
            return Angle.Left;
        } else if (index == 1) {
            return Angle.Right;
        } else if (index == 2) {
            return Angle.Back;
        } else {
            return Angle.Front;
        }
    }
}
