package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * The gas-sensor array's analysis functions over one multi-sensor reading
 * (CD-Fn1..4). The target chemical is configured at startup.
 */
public final class GasSensorArray {

    private final Chem target;

    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): gasD if some sensor reports the target chemical
     * with a positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSample> gs) {
        Status result = Status.noGas;
        for (GasSample sample : gs) {
            if (sample.c().equals(target) && !goreq(0.0, sample.i())) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * Peak intensity of a reading (CD-Fn2). Precondition: the reading is non-empty;
     * an empty reading yields 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> gs) {
        double peak = 0.0;
        boolean first = true;
        for (GasSample sample : gs) {
            if (first || !goreq(peak, sample.i())) {
                peak = sample.i();
                first = false;
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor with the peak intensity (CD-Fn3). Precondition: the
     * reading is non-empty; an empty reading yields the direction of position 1.
     */
    public Angle location(List<GasSample> gs) {
        int strongest = 0;
        double peak = 0.0;
        for (int index = 0; index < gs.size(); index++) {
            double value = gs.get(index).i();
            if (index == 0 || !goreq(peak, value)) {
                peak = value;
                strongest = index;
            }
        }
        return angle(strongest + 1);
    }

    /**
     * Intensity comparison (CD-Fn4): true iff {@code first} is at least {@code second}.
     */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private Angle angle(int position) {
        int sector = (position - 1) % 4;
        Angle result = Angle.Front;
        if (sector == 0) {
            result = Angle.Left;
        } else if (sector == 1) {
            result = Angle.Right;
        } else if (sector == 2) {
            result = Angle.Back;
        }
        return result;
    }
}
