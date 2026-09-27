package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import java.util.List;

/**
 * Gas-reading functions used by the gas-analysis subsystem: analysis, intensity,
 * location and goreq. Missing data yields safe defaults (noGas, 0.0, Front).
 */
public final class GasSensorService {

    @RoboChartType("nat")
    private final int targetChemical;

    public GasSensorService(@RoboChartType("nat") int targetChemical) {
        this.targetChemical = targetChemical;
    }

    /** Returns gasD iff some sensor reports the target chemical with a positive intensity. */
    public Status analysis(List<GasSensor> readings) {
        for (var sensor : readings) {
            if (sensor.c() == targetChemical && sensor.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity across the reading; 0.0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (var sensor : readings) {
            if (goreq(sensor.i(), peak)) {
                peak = sensor.i();
            }
        }
        return peak;
    }

    /** Direction of the (first) sensor attaining the peak intensity; Front for an empty reading. */
    public Angle location(List<GasSensor> readings) {
        var peak = intensity(readings);
        for (var index = 0; index < readings.size(); index++) {
            if (goreq(readings.get(index).i(), peak)) {
                return directionOfPosition(index + 1);
            }
        }
        return Angle.Front;
    }

    /** True iff the first intensity is at least as large as the second. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /** Sensor positions 1..4 face Left, Right, Back, Front; further positions repeat the cycle. */
    private Angle directionOfPosition(@RoboChartType("nat") int position) {
        var slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Left;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        }
        return Angle.Front;
    }
}
