package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.CommandActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasMeasurement;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem: classifies each gas reading and tells the movement subsystem
 * to keep searching (resume), to steer towards a stronger signal (turn) or to stop.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private List<GasMeasurement> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasSensorArray gasSensors;
    private final CommandActuator commands;

    /** Creates the controller in its initial mode, Reading. */
    public GasAnalysisController(GasSensorArray gasSensors, CommandActuator commands) {
        this.gasSensors = gasSensors;
        this.commands = commands;
    }

    /** The current mode. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** One control cycle; {@code event} is the event of this cycle, or null if there is none. */
    public void step(InputEvent event) {
        boolean gasClassified = sts == Status.gasD;
        boolean peakAtOrAboveThreshold = gasSensors.goreq(ins, DetectorConstants.THR);
        boolean alwaysEnabled = true;

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSING;
                gs = gasEvent.samples();
                sts = gasSensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSING) {
            if (gasClassified) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = gasSensors.intensity(gs);
            } else if (!gasClassified) {
                currentMode = GasAnalysisMode.NO_GAS;
                commands.apply(new OutputEvent.Resume());
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            if (alwaysEnabled) {
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.CONCLUDED;
                commands.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.READING;
                anl = gasSensors.location(gs);
                commands.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Final mode: the source has been found; no further readings are processed.
        }
    }
}
