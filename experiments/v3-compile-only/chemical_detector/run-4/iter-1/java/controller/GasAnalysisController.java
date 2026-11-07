package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasAnalysisSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells the
 * movement subsystem to keep searching (resume), to steer towards the
 * strongest signal (turn) or to halt because the source is found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasAnalysisSensor sensor;
    private final GasAnalysisActuator actuator;

    /** gs: the most recent gas reading, initially empty (CD-GA-Var1). */
    private List<GasSensor> gs = new ArrayList<>();
    /** sts: classification of gs (CD-GA-Var2). */
    private Status sts = Status.noGas;
    /** ins: peak intensity of gs (CD-GA-Var3). */
    @RoboChartType("real") private double ins = 0.0;
    /** anl: direction of the strongest signal of gs (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    /**
     * @param sensor   the functions over gas readings
     * @param actuator the port to the movement subsystem
     */
    public GasAnalysisController(GasAnalysisSensor sensor, GasAnalysisActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle: takes at most one transition of the gas-analysis
     * state machine. {@code event} is the event of this cycle, or
     * {@code null} if there is none.
     */
    public void step(InputEvent event) {
        // Guard predicates.
        boolean readingShowsNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThreshold = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            // CD-GA-Beh2: Reading --gas--> Analysis; entry of Analysis: sts = analysis(gs).
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                gs = gasEvent.reading();
                currentMode = GasAnalysisMode.ANALYSIS;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            // CD-GA-Beh4: Analysis --[sts == noGas] / resume--> NoGas.
            if (readingShowsNoGas) {
                currentMode = GasAnalysisMode.NO_GAS;
                actuator.apply(new OutputEvent.Resume());
            }
            // CD-GA-Beh5: Analysis --[sts == gasD]--> GasDetected; entry: ins = intensity(gs).
            else if (!readingShowsNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            // CD-GA-Beh3: NoGas --> Reading. NoGas is entered only when sts == noGas,
            // so this guard always holds in NoGas.
            if (readingShowsNoGas) {
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            // CD-GA-Beh6: GasDetected --[goreq(ins, thr)] / stop--> final.
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.CONCLUDED;
                actuator.apply(new OutputEvent.Stop());
            }
            // CD-GA-Beh7: GasDetected --[not goreq(ins, thr)] / anl = location(gs); turn!anl--> Reading.
            else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.READING;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Final state: the search has concluded; no further readings are processed.
        }
    }
}
