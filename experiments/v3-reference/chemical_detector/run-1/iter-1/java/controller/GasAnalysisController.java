package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.GasAnalysisOutput;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorService;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer (turn) or stop.
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensorService gasSensor;
    private final GasAnalysisOutput output;

    private List<GasSample> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorService gasSensor, GasAnalysisOutput output) {
        this.gasSensor = gasSensor;
        this.output = output;
    }

    /** Current state of the gas-analysis subsystem. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Performs at most one transition of the gas-analysis subsystem. */
    public void step(InputEvent event) {
        boolean readingHasNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThr = gasSensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.reading();
                sts = gasSensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (readingHasNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                output.apply(new OutputEvent.Resume());
            } else if (!readingHasNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasSensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                output.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasSensor.location(gs);
                output.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Final state: no further readings are processed.
        }
    }
}
