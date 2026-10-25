package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.ChemSensors;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem
 * to keep searching (resume), steer towards the strongest signal (turn), or halt (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final ChemSensors sensors;
    private final Actuator actuator;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(ChemSensors sensors, Actuator actuator) {
        this.sensors = sensors;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the event received in this cycle, or {@code null} when none was received
     */
    public void step(InputEvent event) {
        boolean statusNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = ins >= ChemConstants.THR;

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSIS;
                gs = gasEvent.reading();
                sts = sensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            if (statusNoGas) {
                currentMode = GasAnalysisMode.NO_GAS;
                actuator.apply(new OutputEvent.Resume());
            } else if (!statusNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensors.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.FINAL;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.READING;
                anl = sensors.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.FINAL) {
            // Search concluded: later readings are retained but never analysed.
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.FINAL;
                gs = gasEvent.reading();
            }
        }
    }
}
