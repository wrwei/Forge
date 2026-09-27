package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import java.util.List;
import java.util.Objects;

/**
 * Sensing surface of the Vehicle: the odometer reading and the gas-reading
 * classification functions used by the gas-analysis subsystem. Every function
 * is total: an empty reading yields a safe default rather than an error.
 */
public final class Sensor {

    private final Chem target;

    @RoboChartType("real")
    private double distance;

    public Sensor(Chem target) {
        this.target = Objects.requireNonNull(target);
    }

    /** Records the latest cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /**
     * Classifies a reading: gasD if some sensor reports the target chemical with a
     * positive intensity, noGas otherwise (including for an empty reading).
     */
    public Status analysis(List<GasSample> gs) {
        for (var sample : gs) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity across a reading: at least every sample's intensity and equal
     * to one of them. An empty reading yields 0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        var peak = gs.get(0).i();
        for (var sample : gs) {
            if (!goreq(peak, sample.i())) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the first sensor whose intensity equals the peak intensity.
     * Sensor position x (1-based) faces direction {@code Angle.values()[(x - 1) % 4]}.
     * An empty reading yields Front.
     */
    public Angle location(List<GasSample> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        var peakIndex = 0;
        for (var x = 1; x < gs.size(); x++) {
            if (!goreq(gs.get(peakIndex).i(), gs.get(x).i())) {
                peakIndex = x;
            }
        }
        return angle(peakIndex + 1);
    }

    /** Intensity ordering: true iff {@code i1} is at least {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    private static Angle angle(int position) {
        var directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
