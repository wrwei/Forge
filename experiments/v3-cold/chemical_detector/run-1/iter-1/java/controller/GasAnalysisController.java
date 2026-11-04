package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.CommandLink;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorArray;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem: classifies each gas reading and tells the
 * movement subsystem to keep searching (resume), to steer toward the
 * strongest signal (turn) or that the source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private List<GasSample> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasSensorArray gasSensors;
    private final CommandLink link;

    /**
     * @param gasSensors the gas-analysis functions
     * @param link where the turn, stop and resume signals are sent
     */
    public GasAnalysisController(GasSensorArray gasSensors, CommandLink link) {
        this.gasSensors = gasSensors;
        this.link = link;
    }

    /**
     * One control cycle of the gas-analysis subsystem.
     *
     * @param event the event received this cycle, or null if there is none
     */
    public void step(InputEvent event) {
        boolean readingHasNoGas = sts == Status.noGas;
        boolean peakReachesThreshold = gasSensors.goreq(ins, DetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.reading();
                sts = gasSensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (readingHasNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                link.apply(new OutputEvent.Resume());
            } else if (!readingHasNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasSensors.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakReachesThreshold) {
                currentMode = GasAnalysisMode.Concluded;
                link.apply(new OutputEvent.Stop());
            } else if (!peakReachesThreshold) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasSensors.location(gs);
                link.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Terminal mode: the search has concluded and no further readings are processed.
        }
    }
}
