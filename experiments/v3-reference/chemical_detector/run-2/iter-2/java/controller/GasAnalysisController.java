package chemical_detector.controller;

import chemical_detector.Angle;
import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.GasSensor;
import chemical_detector.Status;
import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells the
 * movement subsystem to keep searching, turn towards a stronger signal, or stop.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("nat") private int ins = 0;
    private Angle anl = Angle.Front;

    private final ChemicalSensor sensor;
    private final GasAnalysisActuator actuator;

    /**
     * @param sensor   the gas-analysis functions
     * @param actuator the channel to the movement subsystem
     */
    public GasAnalysisController(ChemicalSensor sensor, GasAnalysisActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state of the gas-analysis subsystem. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the event received in this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        boolean statusNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThr = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: adopt the reading and classify it (Analysis entry: sts = analysis(gs)).
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                gs = gasEvent.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: no gas; tell the movement subsystem to resume searching.
            if (statusNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }
            // CD-GA-Beh5: gas detected (GasDetected entry: ins = intensity(gs)).
            else if (!statusNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: return to Reading for the next reading.
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: source found; stop the movement subsystem and conclude.
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            }
            // CD-GA-Beh7: below the threshold; turn towards the strongest signal.
            else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // CD-GA-Beh6: terminal; no further readings are processed.
        }
    }
}
