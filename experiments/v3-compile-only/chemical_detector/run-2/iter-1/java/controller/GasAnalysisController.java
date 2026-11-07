package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasReadingSensor;

import java.util.List;

/**
 * The gas-analysis subsystem: classifies each gas reading and tells the
 * movement subsystem to keep searching (resume), to steer towards the
 * strongest signal (turn) or to halt (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasReadingSensor gasReadingSensor;
    private final Actuator actuator;

    /**
     * @param gasReadingSensor the functions over gas readings
     * @param actuator         receives the turn, stop and resume events
     */
    public GasAnalysisController(GasReadingSensor gasReadingSensor, Actuator actuator) {
        this.gasReadingSensor = gasReadingSensor;
        this.actuator = actuator;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * One control cycle. {@code event} is the event received in this cycle,
     * or {@code null} when none was received.
     */
    public void step(InputEvent event) {
        boolean readingIndicatesGas = sts == Status.gasD;
        boolean peakAtOrAboveThreshold = gasReadingSensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.readings();
                sts = gasReadingSensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (readingIndicatesGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasReadingSensor.intensity(gs);
            } else if (!readingIndicatesGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasReadingSensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Terminal: the search is over; no further readings are processed.
        }
    }
}
