package chemdetector.operation;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.GasSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * CD-Fn2 intensity operation. Computes the peak intensity across a non-empty
 * sequence of GasSensor readings. Returns 0.0 if the sequence is empty so the
 * controller's named predicates remain simple comparisons.
 */
public final class IntensityCompute {

    @RoboChartType("real")
    private double ins = 0.0;
    private List<GasSensor> input = new ArrayList<>();

    public IntensityCompute() {
    }

    public void setInput(List<GasSensor> gs) {
        this.input = gs;
    }

    public void compute() {
        this.ins = peak(this.input);
    }

    @RoboChartType("real")
    public double ins() {
        return ins;
    }

    @RoboChartType("real")
    private double peak(List<GasSensor> gs) {
        double m = 0.0;
        for (int i = 0; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (v > m) {
                m = v;
            }
        }
        return m;
    }
}
