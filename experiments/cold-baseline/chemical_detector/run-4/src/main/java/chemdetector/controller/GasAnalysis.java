package chemdetector.controller;

import chemdetector.constants.ChemDetectorConstants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import chemdetector.event.GasAnalysisInputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.GasSensorService;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis state machine (CD-GA-FR1..4, CD-GA-Beh1..7).
 *
 * Modes: Reading (initial) -> Analysis -> {NoGas, GasDetected} -> ... -> Final
 *
 * State variables:
 *   gs  : Seq(GasSensor) — most recent reading (CD-GA-Var1)
 *   sts : Status         — last classification result (CD-GA-Var2)
 *   ins : Intensity      — peak intensity of current reading (CD-GA-Var3)
 *   anl : Angle          — direction of strongest sensor (CD-GA-Var4)
 *
 * Each step() call advances at most one transition. Emits to Movement
 * via the shared turn/stop/resume events.
 */
public final class GasAnalysis {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensorService sensor;
    private final Movement movement;

    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    private Intensity ins = new Intensity(0.0);
    private Angle anl = Angle.Front;

    public GasAnalysis(GasSensorService sensor, Movement movement) {
        this.sensor = sensor;
        this.movement = movement;
    }

    public GasAnalysisMode currentMode() { return currentMode; }
    public Status sts() { return sts; }
    public Intensity ins() { return ins; }
    public Angle anl() { return anl; }
    public List<GasSensor> gs() { return gs; }

    public void step(GasAnalysisInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean stsNoGas = sts == Status.noGas;
        boolean stsGasD = sts == Status.gasD;
        boolean insAboveThr = sensor.goreq(ins, ChemDetectorConstants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: Reading -- gas ? gs --> Analysis
            if (event instanceof GasAnalysisInputEvent.Gas) {
                GasAnalysisInputEvent.Gas g = (GasAnalysisInputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                // Entry action of Analysis (inlined): sts = analysis(gs)
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: Analysis [sts==noGas] / send resume --> NoGas
            if (stsNoGas) {
                movement.receive(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            }
            // CD-GA-Beh5: Analysis [sts==gasD] --> GasDetected
            else if (stsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                // Entry action of GasDetected (inlined): ins = intensity(gs)
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: autonomous NoGas --> Reading
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: GasDetected [goreq(ins, thr)] / send stop --> Final
            if (insAboveThr) {
                movement.receive(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Final;
            }
            // CD-GA-Beh7: GasDetected [!goreq(ins, thr)] / anl=location(gs); turn!anl --> Reading
            else {
                anl = sensor.location(gs);
                movement.receive(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Final) {
            // Terminal. No outgoing transitions.
        }
    }
}
