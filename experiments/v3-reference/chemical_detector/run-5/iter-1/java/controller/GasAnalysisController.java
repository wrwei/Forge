package chemical_detector.controller;

import chemical_detector.actuator.MovementLink;
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
 * The gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer towards the strongest signal (turn)
 * or halt because the chemical source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasSensorArray gasSensors;
    private final MovementLink movementLink;

    /**
     * Creates the gas-analysis subsystem in its initial mode, Reading.
     *
     * @param gasSensors the functions over gas readings
     * @param movementLink the link carrying turn, stop and resume to the movement subsystem
     */
    public GasAnalysisController(GasSensorArray gasSensors, MovementLink movementLink) {
        this.gasSensors = gasSensors;
        this.movementLink = movementLink;
    }

    /**
     * The current mode.
     *
     * @return the mode the subsystem is in
     */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle: takes at most one transition of the current mode.
     *
     * @param event the event received in this cycle, or null if there is none
     */
    public void step(InputEvent event) {
        boolean readingHasNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThr = gasSensors.goreq(ins, ChemicalDetectorConstants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas reading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = reading.readings();
                sts = gasSensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (readingHasNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                movementLink.apply(new OutputEvent.Resume());
            } else if (!readingHasNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasSensors.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                movementLink.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasSensors.location(gs);
                movementLink.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Terminal mode: the search has concluded and no further readings are processed.
        }
    }
}
