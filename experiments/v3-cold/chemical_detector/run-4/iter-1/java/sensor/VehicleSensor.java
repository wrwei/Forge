package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Sensing surface of the Vehicle: the odometer, and the functions that
 * classify and evaluate a multi-sensor gas reading.
 */
public final class VehicleSensor {
    private final Chem target;
    @RoboChartType("real") private double distance = 0.0;

    /**
     * @param target the chemical species the robot is searching for
     */
    public VehicleSensor(Chem target) {
        this.target = target;
    }

    /**
     * Records the cumulative distance reported by the odometer.
     *
     * @param distance the distance travelled so far
     */
    public void recordDistance(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /**
     * The odometer: the cumulative distance the Vehicle has travelled.
     *
     * @return the most recently reported distance
     */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /**
     * Classifies a gas reading.
     *
     * @param readings the reading to classify
     * @return {@code gasD} if some sensor reports the target chemical with a
     *     positive intensity, {@code noGas} otherwise
     */
    public Status analysis(List<GasSensor> readings) {
        for (GasSensor reading : readings) {
            if (reading.c().equals(target) && reading.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * The peak intensity of a reading.
     *
     * @param readings a non-empty reading
     * @return the largest {@code i} in the reading; 0.0 if it is empty
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        double peak = readings.get(0).i();
        for (GasSensor reading : readings) {
            if (!goreq(peak, reading.i())) {
                peak = reading.i();
            }
        }
        return peak;
    }

    /**
     * The direction of the strongest signal in a reading.
     *
     * @param readings a non-empty reading
     * @return the angle of the first sensor whose intensity equals the peak;
     *     {@code Front} if the reading is empty
     */
    public Angle location(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        double peak = intensity(readings);
        int position = 1;
        for (GasSensor reading : readings) {
            if (goreq(reading.i(), peak)) {
                return angleAt(position);
            }
            position = position + 1;
        }
        return Angle.Front;
    }

    /**
     * Intensity comparison.
     *
     * @param first an intensity
     * @param second an intensity
     * @return {@code true} iff {@code first} is at least {@code second}
     */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    // angle(x): the sensing direction of the sensor at 1-based position x,
    // in the order of the Angle enumeration, repeating every four sensors.
    private Angle angleAt(int position) {
        int slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Left;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else {
            return Angle.Front;
        }
    }
}
