package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/**
 * Domain functions over a gas reading (CD-Fn1 to CD-Fn4). The position of a
 * value in the reading (1-based) gives its sensing direction.
 */
public final class GasReadingSensor {

    private final Chem target;

    /**
     * @param target the chemical whose source the robot is looking for
     */
    public GasReadingSensor(Chem target) {
        this.target = target;
    }

    /**
     * CD-Fn1: gasD if some value of the reading is for the target chemical
     * with a positive intensity, noGas otherwise (including an empty reading).
     */
    public Status analysis(List<GasSensor> sample) {
        Status result = Status.noGas;
        for (GasSensor value : sample) {
            if (value.c().equals(target) && value.i() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * CD-Fn2: the maximum intensity across the reading. Precondition: the
     * reading is non-empty; an empty reading yields 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> sample) {
        if (sample.isEmpty()) {
            return 0.0;
        }
        double peak = sample.get(0).i();
        for (GasSensor value : sample) {
            if (!goreq(peak, value.i())) {
                peak = value.i();
            }
        }
        return peak;
    }

    /**
     * CD-Fn3: the direction of the first value whose intensity equals the
     * peak intensity. Precondition: the reading is non-empty; an empty
     * reading yields Front.
     */
    public Angle location(List<GasSensor> sample) {
        if (sample.isEmpty()) {
            return Angle.Front;
        }
        double peak = intensity(sample);
        int position = 1;
        boolean located = false;
        for (int index = 0; index < sample.size(); index++) {
            if (!located && sample.get(index).i() == peak) {
                position = index + 1;
                located = true;
            }
        }
        return angle(position);
    }

    /** CD-Fn4: true iff the first intensity is at least the second. */
    public boolean goreq(@RoboChartType("real") double candidate, @RoboChartType("real") double reference) {
        return candidate >= reference;
    }

    /**
     * Maps a 1-based sensor position to its direction: 1 Left, 2 Right,
     * 3 Back, 4 Front, then repeating.
     */
    private Angle angle(int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
