package chemdetector.controller.gas;

import java.util.ArrayList;
import java.util.List;
import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.sensor.Sensor;

/**
 * Gas-analysis subsystem: classifies the latest gas reading and either
 * tells the movement subsystem to keep searching, to turn towards a
 * stronger signal, or to stop because the chemical source has been
 * found.
 *
 * Mode-nested single-method state machine, per the codegen rules.
 */
public final class GasAnalysisController {

    private GasMode currentMode = GasMode.Reading;

    private final Sensor sensor;
    private final Actuator actuator;

    // State variables (CD-GA-Var1..4)
    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    private Intensity ins = new Intensity(0.0);
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasMode currentMode() { return currentMode; }
    public Status sts() { return sts; }
    public Intensity ins() { return ins; }
    public Angle anl() { return anl; }

    public void step(InputEvent event) {
        // --- Named boolean predicates (CRITICAL: extractable guard names) ---
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean intensityAboveThr = sensor.goreq(ins, Constants.thr);

        if (currentMode == GasMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas ge = (InputEvent.Gas) event;
                gs = sensor.copyReading(ge.payload());
                currentMode = GasMode.Analysis;
                // Entry action of Analysis: sts = analysis(gs)
                sts = sensor.analysis(gs);
            }
        } else if (currentMode == GasMode.Analysis) {
            // Autonomous guarded transitions out of Analysis
            if (stsIsNoGas) {
                actuator.apply(new OutputEvent.Resume());
                currentMode = GasMode.NoGas;
            } else if (stsIsGasD) {
                currentMode = GasMode.GasDetected;
                // Entry action of GasDetected: ins = intensity(gs)
                ins = sensor.intensity(gs);
            }
        } else if (currentMode == GasMode.NoGas) {
            // Autonomous transition back to Reading
            currentMode = GasMode.Reading;
        } else if (currentMode == GasMode.GasDetected) {
            // Two autonomous guarded transitions
            if (intensityAboveThr) {
                actuator.apply(new OutputEvent.Stop());
                currentMode = GasMode.Final;
            } else {
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
                currentMode = GasMode.Reading;
            }
        } else if (currentMode == GasMode.Final) {
            // Terminal state: no outgoing transitions
        }
    }
}
