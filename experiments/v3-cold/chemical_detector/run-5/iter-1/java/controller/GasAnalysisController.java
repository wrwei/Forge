package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.AnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer towards the strongest signal
 * (turn) or halt because the source is found (stop).
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;
    private final ChemicalSensor sensor;
    private final AnalysisActuator actuator;

    public GasAnalysisController(ChemicalSensor sensor, AnalysisActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the event received in this cycle, or null if none arrived
     */
    public void step(InputEvent event) {
        // --- Guard predicates ---
        boolean classifiedNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThreshold = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        // --- One block per state; transitions in priority order ---
        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                gs = gasEvent.sample();
                currentMode = GasAnalysisMode.ANALYSING;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSING) {
            if (classifiedNoGas) {
                actuator.apply(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NO_GAS;
            } else if (!classifiedNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            // Autonomous, unguarded: back to Reading for the next reading.
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (peakAtOrAboveThreshold) {
                actuator.apply(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.CONCLUDED;
            } else if (!peakAtOrAboveThreshold) {
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Final: the source is found; no further readings are processed.
        }
    }
}
