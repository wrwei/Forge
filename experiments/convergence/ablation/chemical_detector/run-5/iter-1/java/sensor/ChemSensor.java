package chemdetector.sensor;

import java.util.List;

import chemdetector.annotation.RoboChartType;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;

/**
 * Gas-analysis computations over a multi-sensor reading (CD-Fn1..4).
 * Returns safe defaults for empty readings so callers never need
 * sentinel checks.
 */
public final class ChemSensor {

    private final Chem target;

    public ChemSensor(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading: gasD if any sample reports the target
     * chemical, noGas otherwise (CD-Fn1).
     */
    public Status analysis(List<GasSensor> gs) {
        for (int k = 0; k < gs.size(); k++) {
            if (gs.get(k).c() == target) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading (CD-Fn2). Returns 0.0 for an
     * empty reading (safe default; the spec precondition is non-empty).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double max = 0.0;
        for (int k = 0; k < gs.size(); k++) {
            if (gs.get(k).i() > max) {
                max = gs.get(k).i();
            }
        }
        return max;
    }

    /**
     * Direction of the sensor with the highest intensity (CD-Fn3).
     */
    public Angle location(List<GasSensor> gs) {
        int best = 0;
        for (int k = 1; k < gs.size(); k++) {
            if (gs.get(k).i() > gs.get(best).i()) {
                best = k;
            }
        }
        return angle(best);
    }

    /**
     * Maps a sensor index to its sensing direction (CD-Fn3).
     */
    public Angle angle(@RoboChartType("nat") int index) {
        if (index == 0) {
            return Angle.Left;
        }
        if (index == 1) {
            return Angle.Right;
        }
        if (index == 2) {
            return Angle.Back;
        }
        return Angle.Front;
    }

    /**
     * Intensity greater-or-equal comparison (CD-Fn4).
     */
    public boolean goreq(@RoboChartType("real") double a, @RoboChartType("real") double b) {
        return a >= b;
    }
}
