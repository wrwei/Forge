package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.AnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells
 * the movement subsystem to keep searching (resume), steer towards the
 * strongest signal (turn) or halt (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasSensorArray gasSensors;
    private final AnalysisActuator actuator;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    /**
     * Creates the subsystem in its initial state, Reading (CD-GA-Beh1).
     *
     * @param gasSensors the gas-sensor array and its analysis functions
     * @param actuator the link to the movement subsystem
     */
    public GasAnalysisController(GasSensorArray gasSensors, AnalysisActuator actuator) {
        this.gasSensors = gasSensors;
        this.actuator = actuator;
    }

    /**
     * The current state.
     *
     * @return the current mode
     */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle: takes at most one transition.
     *
     * @param event the event of this cycle, if any
     */
    public void step(InputEvent event) {
        boolean classifiedNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThreshold = gasSensors.goreq(ins, DetectorConstants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSING;
                gs = gasEvent.samples();
                sts = gasSensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSING) {
            if (classifiedNoGas) {
                currentMode = GasAnalysisMode.NO_GAS;
                actuator.apply(new OutputEvent.Resume());
            } else if (!classifiedNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = gasSensors.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.CONCLUDED;
                actuator.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.READING;
                anl = gasSensors.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Terminal: the search has concluded.
        }
    }
}
