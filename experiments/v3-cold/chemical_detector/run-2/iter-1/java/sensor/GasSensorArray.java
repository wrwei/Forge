package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * The gas-sensor array: classifies multi-directional gas readings.
 * Position k (1-based) of a reading is the value of sensor k, which faces
 * the k-th direction of {@link Angle} (Left, Right, Back, Front), wrapping
 * round for arrays with more than four sensors.
 */
public final class GasSensorArray {
    private final Chem target;

    /**
     * Creates the array for one target chemical.
     *
     * @param target the chemical whose source the robot looks for
     */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): {@code gasD} if some sensor reports the
     * target chemical with a positive intensity, {@code noGas} otherwise.
     *
     * @param readings the reading to classify
     * @return the classification
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
     * Peak intensity of a reading (CD-Fn2): the largest {@code i} in it.
     * The reading must be non-empty; an empty reading yields 0.0.
     *
     * @param readings a non-empty reading
     * @return the maximum intensity
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        double peak = readings.get(0).i();
        for (GasSample sample : readings) {
            if (goreq(sample.i(), peak)) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the strongest signal (CD-Fn3): the angle of the first
     * sensor whose intensity equals {@link #intensity(List)}.
     * The reading must be non-empty; an empty reading yields {@code Front}.
     *
     * @param readings a non-empty reading
     * @return the direction of the sensor with the highest intensity
     */
    public Angle location(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        int strongest = 0;
        for (int k = 1; k < readings.size(); k++) {
            if (readings.get(k).i() > readings.get(strongest).i()) {
                strongest = k;
            }
        }
        return directionOfSensor(strongest);
    }

    /**
     * Intensity comparison (CD-Fn4).
     *
     * @param i1 the first intensity
     * @param i2 the second intensity
     * @return true iff {@code i1} is at least {@code i2}
     */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    private static Angle directionOfSensor(int index) {
        Angle[] directions = Angle.values();
        return directions[index % directions.length];
    }
}
