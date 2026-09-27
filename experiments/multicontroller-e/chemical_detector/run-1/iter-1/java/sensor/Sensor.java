package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import java.util.List;

/**
 * Sensing surface of the Vehicle: the odometer and the functions the
 * gas-analysis subsystem applies to a multi-sensor gas reading. Every function
 * returns a safe default for an empty reading.
 */
public final class Sensor {

    private final Chem target;
    @RoboChartType("real")
    private double travelled;

    public Sensor(Chem target) {
        this.target = target;
    }

    /** Records the Vehicle's latest cumulative distance travelled. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /**
     * Classifies a reading: {@code gasD} when some sample reports the target
     * chemical at a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSample> gs) {
        for (var sample : gs) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity of a reading; {@code 0} for an empty reading. */
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
     * Direction of the sensor that reported the peak intensity (the first such
     * sensor on a tie); {@code Front} for an empty reading.
     */
    public Angle location(List<GasSample> gs) {
        var best = 0;
        for (var x = 1; x < gs.size(); x++) {
            if (!goreq(gs.get(best).i(), gs.get(x).i())) {
                best = x;
            }
        }
        return angle(best + 1);
    }

    /** Intensity ordering: true iff {@code i1} is at least {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    /** Sensing direction of the sensor at 1-based position {@code x}, clockwise from Front. */
    private static Angle angle(@RoboChartType("nat") int x) {
        var slot = (x - 1) % 4;
        if (slot == 0) {
            return Angle.Front;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }
}
