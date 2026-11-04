package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.VehicleSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to resume searching, turn toward the strongest signal, or stop.
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;
    private final VehicleSensor sensor;
    private final Actuator actuator;

    /**
     * @param sensor the Vehicle's sensing surface
     * @param actuator the output through which turn, stop and resume are sent
     */
    public GasAnalysisController(VehicleSensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** @return the current mode */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or {@code null} if none
     */
    public void step(InputEvent event) {
        boolean readingIndicatesNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThreshold = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.value();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (readingIndicatesNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (!readingIndicatesNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // Autonomous transition with no trigger and no guard.
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThreshold) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Terminal: the search has concluded; no further readings are processed.
        }
    }
}
