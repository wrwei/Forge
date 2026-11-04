package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * The gas-sensor array: the functions the gas-analysis subsystem applies to a
 * multi-sensor reading (CD-Fn1 to CD-Fn4).
 */
public final class GasSensorArray {

    private final Chem target;

    /**
     * Creates the array for one target chemical, configured at startup.
     *
     * @param target the chemical whose source the robot searches for
     */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): {@code gasD} if some value identifies the
     * target chemical with a positive intensity, {@code noGas} otherwise.
     *
     * @param samples the reading
     * @return the classification
     */
    public Status analysis(List<GasSensor> samples) {
        for (GasSensor sample : samples) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * The peak intensity of a reading (CD-Fn2): at least every value's
     * intensity and equal to one of them. The requirement assumes a non-empty
     * reading; an empty one yields 0.0.
     *
     * @param samples the reading
     * @return the maximum intensity
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> samples) {
        if (samples.isEmpty()) {
            return 0.0;
        }
        double peak = samples.get(0).i();
        for (GasSensor sample : samples) {
            if (!goreq(peak, sample.i())) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * The direction of the strongest value of a reading (CD-Fn3): the angle of
     * the first position whose intensity equals {@link #intensity(List)}. The
     * requirement assumes a non-empty reading; an empty one yields Front.
     *
     * @param samples the reading
     * @return the angle of the sensor with the highest intensity
     */
    public Angle location(List<GasSensor> samples) {
        double peak = intensity(samples);
        for (int index = 0; index < samples.size(); index++) {
            if (samples.get(index).i() == peak) {
                return angle(index + 1);
            }
        }
        return Angle.Front;
    }

    /**
     * Intensity comparison (CD-Fn4): true iff the first intensity is at least
     * the second.
     *
     * @param first the intensity compared
     * @param second the intensity compared against
     * @return {@code first >= second}
     */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    // Sensing direction of a 1-based position: Front, Right, Back, Left, repeating.
    private static Angle angle(int position) {
        int slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Front;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else {
            return Angle.Left;
        }
    }
}
