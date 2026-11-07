package chemical_detector.controller;

import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasAnalysisSensor;
import chemical_detector.types.Angle;
import chemical_detector.types.GasSample;
import chemical_detector.types.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-ARCH2): classifies each gas reading and
 * tells the movement subsystem to resume searching, turn towards the
 * strongest signal, or stop because the source has been found.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;

    /** The most recent gas reading (CD-GA-Var1). */
    private List<GasSample> gs = new ArrayList<>();
    /** Classification of gs (CD-GA-Var2). */
    private Status sts = Status.noGas;
    /** Peak intensity of gs (CD-GA-Var3). */
    @RoboChartType("real") private double ins = 0.0;
    /** Direction of the strongest signal in gs (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    private final GasAnalysisSensor sensor;
    private final GasAnalysisActuator actuator;

    /** Creates the subsystem in its initial state, Reading (CD-GA-Beh1). */
    public GasAnalysisController(GasAnalysisSensor sensor, GasAnalysisActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle with the event received in it (null if none). */
    public void step(InputEvent event) {
        // Guard predicates.
        boolean noGasClassified = sts == Status.noGas;
        boolean peakAtThreshold = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            // CD-GA-Beh2: Reading --gas--> Analysis; entry of Analysis: sts = analysis(gs).
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasReading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSIS;
                gs = gasReading.reading();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            // CD-GA-Beh4: Analysis --[sts == noGas] / resume--> NoGas.
            if (noGasClassified) {
                currentMode = GasAnalysisMode.NO_GAS;
                actuator.apply(new OutputEvent.Resume());
            // CD-GA-Beh5: Analysis --[sts == gasD]--> GasDetected; entry: ins = intensity(gs).
            } else if (!noGasClassified) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            // CD-GA-Beh3: NoGas --> Reading. NoGas is entered only with sts == noGas
            // and nothing changes sts there, so this guard always holds in NoGas.
            if (noGasClassified) {
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            // CD-GA-Beh6: GasDetected --[ins >= thr] / stop--> Concluded.
            if (peakAtThreshold) {
                currentMode = GasAnalysisMode.CONCLUDED;
                actuator.apply(new OutputEvent.Stop());
            // CD-GA-Beh7: GasDetected --[ins < thr] / anl = location(gs); turn!anl--> Reading.
            } else if (!peakAtThreshold) {
                currentMode = GasAnalysisMode.READING;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Terminal: the search is over and no further readings are processed.
        }
    }
}
