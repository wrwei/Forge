package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Gas-analysis subsystem. Classifies each gas reading and tells the movement
 * subsystem to keep searching ({@code resume}), to head towards the strongest
 * signal ({@code turn}), or to halt because the source is found ({@code stop}).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final Sensor sensor;
    private final Actuator actuator;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Runs one control cycle. {@code event} is the input received in this
     * cycle, or {@code null} when none arrived.
     */
    public void step(InputEvent event) {
        boolean stsNoGas = sts == Status.noGas;
        boolean insAtLeastThr = sensor.goreq(ins, Constants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas received = (InputEvent.gas) event;
                gs = received.gs();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.resume());
            } else if (!stsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtLeastThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.stop());
            } else if (!insAtLeastThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas received = (InputEvent.gas) event;
                gs = received.gs();
                currentMode = GasAnalysisMode.Concluded;
            }
        }
    }
}
