package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;

import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to resume searching, turn towards a stronger signal, or stop
 * because the chemical source has been found.
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final ChemicalSensor sensor;
    private final Actuator actuator;

    /**
     * @param sensor   the gas-reading functions
     * @param actuator the port carrying turn, stop and resume to movement
     */
    public GasAnalysisController(ChemicalSensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state of the subsystem. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or null if none
     */
    public void step(InputEvent event) {
        boolean stsNoGas = sts == Status.noGas;
        boolean stsGasD = sts == Status.gasD;
        boolean insAtOrAboveThr = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasReading = (InputEvent.Gas) event;
                gs = gasReading.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsNoGas) {
                actuator.apply(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (stsGasD && !stsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                actuator.apply(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Final;
            } else if (!insAtOrAboveThr) {
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Final) {
            // Terminal: the search has concluded and no further readings are processed.
        }
    }
}
