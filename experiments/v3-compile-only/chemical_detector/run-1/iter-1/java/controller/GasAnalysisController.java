package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-GA-*): classifies each gas reading and tells
 * the movement subsystem to resume searching, turn towards the strongest
 * signal, or stop because the source is found.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final ChemicalSensor sensor;
    private final GasAnalysisActuator actuator;

    /** The most recent gas reading; initially empty (CD-GA-Var1). */
    private List<GasSample> gs = new ArrayList<>();
    /** Classification of gs (CD-GA-Var2). */
    private Status sts = Status.noGas;
    /** Peak intensity of gs (CD-GA-Var3). */
    @RoboChartType("real") private double ins = 0.0;
    /** Direction of the strongest signal in gs (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    /** Creates the controller in its initial state, Reading (CD-GA-Beh1). */
    public GasAnalysisController(ChemicalSensor sensor, GasAnalysisActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or null when none
     */
    public void step(InputEvent event) {
        boolean statusIsNoGas = sts == Status.noGas;
        boolean peakIntensityAtOrAboveThr = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: gas(gs), then Analysis entry sts = analysis(gs)
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.reading();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: [sts == noGas] / resume
            if (statusIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }
            // CD-GA-Beh5: [sts == gasD], then GasDetected entry ins = intensity(gs)
            else if (!statusIsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: no trigger, no guard, no action
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: [goreq(ins, thr)] / stop
            if (peakIntensityAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            }
            // CD-GA-Beh7: [not goreq(ins, thr)] / anl = location(gs); turn!anl
            else if (!peakIntensityAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Final: the search is over and no further readings are processed.
        }
    }
}
