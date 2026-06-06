package chemdetector.operation;

import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import chemdetector.sensor.GasSensorService;
import java.util.ArrayList;
import java.util.List;

/**
 * CD-Fn1 analysis operation. Classifies the most recent gas reading as
 * {@link Status#gasD} when at least one reading has positive intensity, otherwise
 * {@link Status#noGas}. The 'target chemical present' criterion is encoded as
 * 'any positive intensity' — the threshold check is performed separately by the
 * GasDetected state's outgoing transitions.
 */
public final class Analysis {

    private final GasSensorService sensor;
    private Status sts = Status.noGas;
    private List<GasSensor> input = new ArrayList<>();

    public Analysis(GasSensorService sensor) {
        this.sensor = sensor;
    }

    public void setInput(List<GasSensor> gs) {
        this.input = gs;
    }

    public void compute() {
        this.sts = classify(this.input);
    }

    public Status sts() {
        return sts;
    }

    private Status classify(List<GasSensor> gs) {
        Status result = Status.noGas;
        for (int i = 0; i < gs.size(); i++) {
            Intensity v = gs.get(i).i();
            if (v.value() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }
}
