package chemical_detector.controller;

import chemical_detector.actuator.MovementLinkActuator;
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
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells the
 * movement subsystem to keep searching, turn towards the signal, or stop.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSample> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasSensorArray sensor;
    private final MovementLinkActuator link;

    /** Creates the subsystem over its gas-sensor array and its link to the movement subsystem. */
    public GasAnalysisController(GasSensorArray sensor, MovementLinkActuator link) {
        this.sensor = sensor;
        this.link = link;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** One control cycle: handles the given event (or none) and autonomous transitions. */
    public void step(InputEvent event) {
        // --- Named guard predicates ---
        boolean readingIndicatesGas = sts == Status.gasD;
        boolean peakAtOrAboveThreshold = sensor.goreq(ins, DetectorConstants.THR);
        // The transition from NoGas back to Reading has no guard (CD-GA-Beh3).
        boolean readyForNextReading = true;

        // --- One block per state ---
        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas reading = (InputEvent.Gas) event;
                gs = reading.readings();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (readingIndicatesGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            } else if (!readingIndicatesGas) {
                currentMode = GasAnalysisMode.NoGas;
                link.apply(new OutputEvent.Resume());
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            if (readyForNextReading) {
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.SourceLocated;
                link.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                link.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.SourceLocated) {
            // Terminal: the search has concluded and no further readings are processed.
        }
    }
}
