package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to resume searching, turn towards a stronger signal, or stop.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final Sensor sensor;
    private final Actuator actuator;

    private List<GasSample> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins;
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Executes one control cycle, reacting to {@code event}. */
    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = sensor.goreq(ins, Constants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas reading = (InputEvent.Gas) event;
                gs = reading.gs();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (!stsIsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // The search is over: later readings are retained but never analysed.
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas reading = (InputEvent.Gas) event;
                gs = reading.gs();
                currentMode = GasAnalysisMode.Concluded;
            }
        }
    }

    List<GasSample> gs() {
        return gs;
    }

    Status sts() {
        return sts;
    }

    @RoboChartType("real")
    double ins() {
        return ins;
    }

    Angle anl() {
        return anl;
    }
}
