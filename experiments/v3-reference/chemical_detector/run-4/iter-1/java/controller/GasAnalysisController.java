package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to resume searching, turn towards the strongest signal, or stop.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSample> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasSensorArray gasSensorArray;
    private final GasAnalysisActuator actuator;

    public GasAnalysisController(GasSensorArray gasSensorArray, GasAnalysisActuator actuator) {
        this.gasSensorArray = gasSensorArray;
        this.actuator = actuator;
    }

    /** The current state of the subsystem. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Performs one control cycle; {@code event} is the event received, or null. */
    public void step(InputEvent event) {
        boolean statusNoGas = sts == Status.noGas;
        boolean statusGasDetected = sts == Status.gasD;
        boolean peakAtOrAboveThr = gasSensorArray.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasReading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasReading.reading();
                sts = gasSensorArray.analysis(gs);
            }
        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (statusNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (statusGasDetected && !statusNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasSensorArray.intensity(gs);
            }
        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;
        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasSensorArray.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }
        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Final state: the source has been found and no further readings are processed.
        }
    }
}
