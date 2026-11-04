package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.CommandLink;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem to keep
 * searching (resume), steer toward the strongest signal (turn) or halt (stop).
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasSensorArray sensor;
    private final CommandLink link;

    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    /** Creates the subsystem reading the given sensor array and commanding through the given link. */
    public GasAnalysisController(GasSensorArray sensor, CommandLink link) {
        this.sensor = sensor;
        this.link = link;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Performs one control cycle; {@code event} is the event received in this cycle, if any. */
    public void step(InputEvent event) {
        boolean noGasClassified = sts == Status.noGas;
        boolean gasClassified = sts == Status.gasD;
        boolean peakAtOrAboveThreshold = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSIS;
                gs = gasEvent.reading();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            if (noGasClassified) {
                currentMode = GasAnalysisMode.NO_GAS;
                link.apply(new OutputEvent.Resume());
            } else if (gasClassified && !noGasClassified) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.CONCLUDED;
                link.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.READING;
                anl = sensor.location(gs);
                link.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Terminal: the search has concluded and no further readings are processed.
        }
    }
}
