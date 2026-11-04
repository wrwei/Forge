package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * The gas-sensor array's derived quantities: the functions the gas-analysis
 * controller applies to the most recent multi-sensor reading.
 */
public final class GasSensorArray {

    private final Chem target;

    /**
     * @param target the chemical species the robot is searching for
     */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading: gasD if some sample is of the target chemical with
     * a positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSample> readings) {
        for (GasSample sample : readings) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * The peak intensity of a reading: at least every sample's intensity and
     * equal to one of them. Precondition: the reading is non-empty; for an
     * empty reading the result is 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        return readings.get(strongest(readings)).i();
    }

    /**
     * The direction of the sample with the peak intensity (the first such
     * sample if several share it). Precondition: the reading is non-empty; for
     * an empty reading the result is Front.
     */
    public Angle location(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        return angle(strongest(readings) + 1);
    }

    /**
     * Whether intensity {@code i1} is at least intensity {@code i2}.
     */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    // 0-based index of the first sample with the highest intensity; readings is non-empty.
    private static int strongest(List<GasSample> readings) {
        int best = 0;
        for (int x = 1; x < readings.size(); x++) {
            if (readings.get(x).i() > readings.get(best).i()) {
                best = x;
            }
        }
        return best;
    }

    // Sensing direction of the sensor at 1-based position: positions 1..4 are
    // Left, Right, Back, Front, and the mapping repeats for longer readings.
    private static Angle angle(int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
