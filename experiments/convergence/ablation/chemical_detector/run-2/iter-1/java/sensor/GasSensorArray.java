package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.Chem;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import java.util.List;

/**
 * Gas-analysis computations over a multi-sensor reading. All methods
 * return safe defaults for an empty reading so callers never need
 * sentinel checks.
 */
public final class GasSensorArray {

    private static final Chem TARGET = Chem.chemA;

    /**
     * Classifies a reading: {@code gasD} if any sample indicates the
     * target chemical, {@code noGas} otherwise (CD-Fn1).
     */
    public Status analysis(List<GasSensor> gs) {
        for (int k = 0; k < gs.size(); k = k + 1) {
            GasSensor sample = gs.get(k);
            if (sample.c() == TARGET && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading; 0.0 for an empty reading
     * (CD-Fn2).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double best = 0.0;
        for (int k = 0; k < gs.size(); k = k + 1) {
            GasSensor sample = gs.get(k);
            if (sample.i() > best) {
                best = sample.i();
            }
        }
        return best;
    }

    /**
     * Direction of the sensor with the highest intensity; {@code Front}
     * for an empty reading (CD-Fn3).
     */
    public Angle location(List<GasSensor> gs) {
        int bestIndex = 0;
        double best = 0.0;
        for (int k = 0; k < gs.size(); k = k + 1) {
            GasSensor sample = gs.get(k);
            if (sample.i() > best) {
                best = sample.i();
                bestIndex = k;
            }
        }
        return angleOf(bestIndex);
    }

    /**
     * Intensity ordering predicate: true iff {@code x >= y} (CD-Fn4).
     */
    public boolean goreq(@RoboChartType("real") double x, @RoboChartType("real") double y) {
        return x >= y;
    }

    private Angle angleOf(int index) {
        if (index == 0) {
            return Angle.Front;
        }
        if (index == 1) {
            return Angle.Left;
        }
        if (index == 2) {
            return Angle.Right;
        }
        return Angle.Back;
    }
}
