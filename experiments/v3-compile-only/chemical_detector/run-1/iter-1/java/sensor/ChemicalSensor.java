package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Gas-analysis functions over a multi-sensor reading (CD-Fn1..4). Position k
 * (1-based) of a reading is the sensor facing direction {@code angle(k)}.
 */
public final class ChemicalSensor {

    @RoboChartType("nat") private final int targetChem;

    /** Creates the functions for the given target chemical identity. */
    public ChemicalSensor(@RoboChartType("nat") int targetChem) {
        this.targetChem = targetChem;
    }

    /**
     * Classifies a reading (CD-Fn1): gasD if some sample is of the target
     * chemical with a positive intensity, noGas otherwise (also when empty).
     */
    public Status analysis(List<GasSample> samples) {
        for (int k = 0; k < samples.size(); k++) {
            GasSample sample = samples.get(k);
            if (sample.c() == targetChem && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a reading (CD-Fn2). Precondition: non-empty. The result
     * is at least every sample's intensity and equals one of them; 0.0 for an
     * empty reading.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> samples) {
        if (samples.isEmpty()) {
            return 0.0;
        }
        double peak = samples.get(0).i();
        for (int k = 1; k < samples.size(); k++) {
            if (samples.get(k).i() > peak) {
                peak = samples.get(k).i();
            }
        }
        return peak;
    }

    /**
     * Direction of the strongest sensor (CD-Fn3). Precondition: non-empty. The
     * result is angle(x) for the first position x whose intensity equals
     * intensity(samples); Front for an empty reading.
     */
    public Angle location(List<GasSample> samples) {
        if (samples.isEmpty()) {
            return Angle.Front;
        }
        int strongest = 0;
        for (int k = 1; k < samples.size(); k++) {
            if (samples.get(k).i() > samples.get(strongest).i()) {
                strongest = k;
            }
        }
        return angle(strongest + 1);
    }

    /** True iff the first intensity is at least the second (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double measured, @RoboChartType("real") double reference) {
        return measured >= reference;
    }

    /** Direction of the sensor at 1-based position {@code position}, cycling through Angle. */
    private Angle angle(int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
