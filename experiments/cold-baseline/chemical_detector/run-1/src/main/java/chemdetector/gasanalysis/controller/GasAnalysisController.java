package chemdetector.gasanalysis.controller;

import chemdetector.actuator.GasAnalysisOutput;
import chemdetector.constants.DetectorConstants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import chemdetector.event.InputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.Sensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem state machine (CD-GA-FR1..4, CD-GA-Beh1..7).
 *
 * Modes: Reading (initial) -> Analysis -> {NoGas | GasDetected} -> ... -> Final.
 *
 * Inputs (boundary):  gas (CD-Evt1)
 * Outputs (shared):   turn, stop, resume (CD-Evt4..6)
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final Sensor sensor;
    private final GasAnalysisOutput out;

    /** CD-GA-Var1: latest gas reading. */
    private List<GasSensor> gs = new ArrayList<>();

    /** CD-GA-Var2: classification result of the current cycle. */
    private Status sts = Status.noGas;

    /** CD-GA-Var3: peak intensity of the current reading. */
    private Intensity ins = new Intensity(0.0);

    /** CD-GA-Var4: angle of the strongest signal in the current reading. */
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, GasAnalysisOutput out) {
        this.sensor = sensor;
        this.out = out;
    }

    public GasAnalysisMode currentMode() { return currentMode; }
    public Status sts() { return sts; }
    public Intensity ins() { return ins; }
    public Angle anl() { return anl; }
    public List<GasSensor> gs() { return gs; }

    public void step(InputEvent event) {
        // Named boolean predicates (declared BEFORE the if-else chain).
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean insAboveThr = sensor.goreq(ins, DetectorConstants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: Reading -> Analysis on gas?gs.
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas ge = (InputEvent.Gas) event;
                gs = ge.payload();
                currentMode = GasAnalysisMode.Analysis;
                // Entry action of Analysis: sts = analysis(gs).
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: Analysis -> NoGas when sts == noGas. Send resume.
            if (stsIsNoGas) {
                out.resume();
                currentMode = GasAnalysisMode.NoGas;
            }
            // CD-GA-Beh5: Analysis -> GasDetected when sts == gasD.
            else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                // Entry action of GasDetected: ins = intensity(gs).
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: NoGas -> Reading. Autonomous, no guard, no action.
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: GasDetected -> Final when goreq(ins, thr). Send stop.
            if (insAboveThr) {
                out.stop();
                currentMode = GasAnalysisMode.Final;
            }
            // CD-GA-Beh7: GasDetected -> Reading otherwise. anl=location(gs); send turn!anl.
            else if (!insAboveThr) {
                anl = sensor.location(gs);
                out.turn(anl);
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Final) {
            // Terminal state — no outgoing transitions.
        }
    }
}
