package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/**
 * Functions over a multi-sensor gas reading used by the gas-analysis subsystem.
 * Every function returns a safe default for an empty reading instead of failing.
 */
public final class GasFunctions {

    @RoboChartType("nat")
    private final int targetChemId;

    /**
     * @param targetChemId identity of the chemical the robot is searching for
     */
    public GasFunctions(@RoboChartType("nat") int targetChemId) {
        this.targetChemId = targetChemId;
    }

    /**
     * Classifies a reading: {@code gasD} if some sensor reports the target
     * chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> readings) {
        for (GasSensor sensor : readings) {
            if (sensor.c().id() == this.targetChemId && sensor.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a reading. Precondition: the reading is non-empty
     * (an empty reading yields 0.0). The result is goreq every sensor's
     * intensity and equals one of them.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        double peak = readings.get(0).i();
        for (GasSensor sensor : readings) {
            if (!goreq(peak, sensor.i())) {
                peak = sensor.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the first sensor whose intensity equals the peak intensity.
     * Precondition: the reading is non-empty (an empty reading yields Front).
     */
    public Angle location(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        double peak = intensity(readings);
        for (int index = 0; index < readings.size(); index++) {
            if (goreq(readings.get(index).i(), peak)) {
                return angle(index + 1);
            }
        }
        return Angle.Front;
    }

    /** True iff {@code first} is at least as large as {@code second}. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /**
     * Sensing direction of the sensor at a 1-based list position. Positions
     * cycle Front, Right, Back, Left; a position below 1 maps to Front.
     */
    public Angle angle(@RoboChartType("nat") int position) {
        if (position < 1) {
            return Angle.Front;
        }
        int slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Front;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else {
            return Angle.Left;
        }
    }
}
