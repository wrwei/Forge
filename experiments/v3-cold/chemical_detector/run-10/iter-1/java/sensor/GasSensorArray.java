package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/** The gas-sensor array's interpretation of a reading: the gas-analysis functions. */
public final class GasSensorArray {

    private final Chem target;

    /** Creates the array tuned to the given target chemical. */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /**
     * CD-Fn1: noGas if no sample indicates the target chemical, gasD otherwise.
     * A sample indicates the target when it is for the target chemical and its
     * intensity is positive.
     */
    public Status analysis(List<GasSample> samples) {
        for (int k = 0; k < samples.size(); k++) {
            GasSample sample = samples.get(k);
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * CD-Fn2: the maximum intensity across a non-empty reading; the result is at
     * least every sample's intensity and equals one of them. An empty reading
     * (outside the precondition) gives 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> samples) {
        if (samples.isEmpty()) {
            return 0.0;
        }
        double peak = samples.get(0).i();
        for (int k = 1; k < samples.size(); k++) {
            double current = samples.get(k).i();
            if (!goreq(peak, current)) {
                peak = current;
            }
        }
        return peak;
    }

    /**
     * CD-Fn3: the direction of the first sensor whose intensity equals the
     * reading's maximum. An empty reading (outside the precondition) gives Front.
     */
    public Angle location(List<GasSample> samples) {
        if (samples.isEmpty()) {
            return Angle.Front;
        }
        double peak = intensity(samples);
        for (int k = 0; k < samples.size(); k++) {
            if (goreq(samples.get(k).i(), peak)) {
                return angleOf(k + 1);
            }
        }
        return angleOf(1);
    }

    /** CD-Fn4: true iff the first intensity is at least the second. */
    public boolean goreq(@RoboChartType("real") double measured, @RoboChartType("real") double reference) {
        return measured >= reference;
    }

    // Sensing direction of sensor position x (1-based): the Angle values in
    // declaration order (Left, Right, Back, Front), repeating beyond four sensors.
    private Angle angleOf(int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
