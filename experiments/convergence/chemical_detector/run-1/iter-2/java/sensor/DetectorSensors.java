package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import java.util.List;

/**
 * Sensor computations of the Chemical Detector: gas-reading
 * classification (CD-Fn1..4) and the odometer reading consumed by stuck
 * detection (CD-Evt3). All methods return safe defaults for missing data.
 */
@SensorService
public final class DetectorSensors {

    @RoboChartType("real")
    private double travelled;

    /** Environment hook: record the cumulative distance travelled. */
    public void update(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Current cumulative distance travelled by the Vehicle (CD-Evt3). */
    @RoboChartType("real")
    public double odometerDistance() {
        return travelled;
    }

    /**
     * Classify a reading (CD-Fn1): gasD if any sensor reports the target
     * chemical with positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSensor> reading) {
        for (int k = 0; k < reading.size(); k++) {
            if (reading.get(k).c() == Chem.TARGET && reading.get(k).i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading (CD-Fn2). Returns 0 for an
     * empty reading (safe default).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> reading) {
        double best = 0.0;
        for (int k = 0; k < reading.size(); k++) {
            if (reading.get(k).i() > best) {
                best = reading.get(k).i();
            }
        }
        return best;
    }

    /**
     * Angle of the sensor with the highest intensity (CD-Fn3). Returns
     * the front direction for an empty reading (safe default).
     */
    public Angle location(List<GasSensor> reading) {
        int bestIndex = 0;
        double best = 0.0;
        for (int k = 0; k < reading.size(); k++) {
            if (reading.get(k).i() > best) {
                best = reading.get(k).i();
                bestIndex = k;
            }
        }
        return angle(bestIndex);
    }

    /** Intensity greater-or-equal comparison (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /** Map a sensing-direction index to an Angle (CD-DM7). */
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
}
