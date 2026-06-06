package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.datatype.GasSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Sensor service exposing the most recent gas reading and derived quantities
 * required by the gas-analysis controller.
 *
 * <p>It exposes the analysis, intensity and location functions referenced by the
 * controller's guards and entry actions. The implementation is the safe-default
 * baseline: when no reading is available the sensor returns sentinel values so
 * the controller's named predicates remain simple comparisons.</p>
 */
@SensorService
public final class GasSensorService {

    private List<GasSensor> currentReading = new ArrayList<>();

    public void update(List<GasSensor> reading) {
        this.currentReading = new ArrayList<>(reading);
    }

    public List<GasSensor> reading() {
        return currentReading;
    }

    /** CD-Fn4: intensity-greater-or-equal. */
    public boolean goreq(@RoboChartType("real") double a, @RoboChartType("real") double b) {
        return a >= b;
    }

    /** CD-Fn2: peak intensity across the reading; 0.0 when empty. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double peak = 0.0;
        for (int i = 0; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (v > peak) {
                peak = v;
            }
        }
        return peak;
    }
}
