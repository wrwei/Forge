package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import java.util.List;

/**
 * Sensing surface of the Vehicle: gas-reading classification functions
 * and the odometer. Returns safe defaults when no data exists.
 */
public final class GasSensorArray {

    private final Chem target;

    @RoboChartType("real")
    private double travelled;

    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /** Updates the cumulative distance reported by the odometer. */
    public void update(@RoboChartType("real") double distanceTravelled) {
        this.travelled = distanceTravelled;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /**
     * Classifies a reading: gasD when some sensor reports the target
     * chemical, noGas otherwise.
     */
    public Status analysis(List<GasSensor> readings) {
        for (int k = 0; k < readings.size(); k = k + 1) {
            if (readings.get(k).c().equals(target)) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading. Returns 0.0 for an empty
     * reading (safe default; intensities are non-negative).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        double max = 0.0;
        for (int k = 0; k < readings.size(); k = k + 1) {
            if (readings.get(k).i() > max) {
                max = readings.get(k).i();
            }
        }
        return max;
    }

    /**
     * Direction of the sensor with the highest intensity in the
     * reading. Returns Front for an empty reading (safe default).
     */
    public Angle location(List<GasSensor> readings) {
        int best = 0;
        for (int k = 0; k < readings.size(); k = k + 1) {
            if (readings.get(k).i() > readings.get(best).i()) {
                best = k;
            }
        }
        return angle(best);
    }

    /** Greater-or-equal ordering predicate on intensities. */
    public boolean goreq(@RoboChartType("real") double x, @RoboChartType("real") double y) {
        return x >= y;
    }

    /** Maps a sensor position in the reading to its sensing direction. */
    private Angle angle(int index) {
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
