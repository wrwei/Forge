package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Domain functions over a gas reading, used by the gas-analysis subsystem.
 */
public final class GasSensorService {
    private final Chem target;

    /** Creates the service for the target chemical configured at startup. */
    public GasSensorService(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading: gasD if some sample is for the target chemical
     * with a positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSample> readings) {
        Status result = Status.noGas;
        for (GasSample sample : readings) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * Peak intensity of a non-empty reading: at least every sample's i and
     * equal to some sample's i. An empty reading yields 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> readings) {
        double peak = 0.0;
        if (!readings.isEmpty()) {
            peak = readings.get(0).i();
        }
        for (GasSample sample : readings) {
            if (goreq(sample.i(), peak)) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor with the highest intensity in a non-empty
     * reading. An empty reading yields Front.
     */
    public Angle location(List<GasSample> readings) {
        Angle result = Angle.Front;
        if (!readings.isEmpty()) {
            int best = 0;
            for (int x = 1; x < readings.size(); x++) {
                if (readings.get(x).i() > readings.get(best).i()) {
                    best = x;
                }
            }
            result = angle(best + 1);
        }
        return result;
    }

    /** True iff the first intensity is at least as large as the second. */
    public boolean goreq(@RoboChartType("real") double lhs, @RoboChartType("real") double rhs) {
        return lhs >= rhs;
    }

    // Maps a 1-based sensor position to its sensing direction, cycling
    // through Left, Right, Back, Front.
    private Angle angle(int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
