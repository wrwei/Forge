package chemdetector.sensor;

import java.util.List;

import chemdetector.annotation.RoboChartType;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;

/**
 * Sensing functions over the Vehicle's raw inputs: gas-reading
 * classification, peak-intensity extraction, strongest-signal
 * direction, intensity comparison, and the odometer reading.
 *
 * <p>All methods return safe defaults for missing data (empty reading
 * sequences) so controller predicates never need sentinel checks.
 */
public final class VehicleSensors {

    private final Chem target;

    @RoboChartType("real")
    private double travelled = 0.0;

    public VehicleSensors(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading: gasD if any sensor reports the target
     * chemical, noGas otherwise (including the empty reading).
     */
    public Status analysis(List<GasSensor> gs) {
        for (int idx = 0; idx < gs.size(); idx++) {
            if (gs.get(idx).c() == target) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading; 0.0 for the empty reading.
     */
    public double intensity(List<GasSensor> gs) {
        double peak = 0.0;
        for (int idx = 0; idx < gs.size(); idx++) {
            if (gs.get(idx).i() > peak) {
                peak = gs.get(idx).i();
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor with the highest intensity; Front for the
     * empty reading.
     */
    public Angle location(List<GasSensor> gs) {
        Angle best = Angle.Front;
        double bestIntensity = -1.0;
        for (int idx = 0; idx < gs.size(); idx++) {
            if (gs.get(idx).i() > bestIntensity) {
                bestIntensity = gs.get(idx).i();
                best = angle(idx);
            }
        }
        return best;
    }

    /**
     * Maps a 0-based sensor index to its sensing direction.
     */
    public Angle angle(@RoboChartType("nat") int index) {
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

    /**
     * Intensity comparison: true iff x is at least as large as y.
     */
    public boolean goreq(@RoboChartType("real") double x, @RoboChartType("real") double y) {
        return x >= y;
    }

    /**
     * Cumulative distance travelled by the Vehicle.
     */
    public double odometer() {
        return travelled;
    }

    /**
     * Feeds a new cumulative-distance value from the platform.
     */
    public void updateOdometer(@RoboChartType("real") double value) {
        this.travelled = value;
    }
}
