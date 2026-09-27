package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells the movement
 * subsystem to keep searching ({@code resume}), steer towards a stronger signal
 * ({@code turn}) or halt because the source is found ({@code stop}).
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

    /** Performs at most one transition of the gas-analysis state machine. */
    public void step(InputEvent event) {
        boolean stsGasD = sts == Status.gasD;
        boolean insReachesThr = sensor.goreq(ins, Constants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.gas) {
                var gasEvent = (InputEvent.gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.reading();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (!stsGasD) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.resume());
            } else if (stsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insReachesThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.stop());
            } else if (!insReachesThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.gas) {
                var gasEvent = (InputEvent.gas) event;
                currentMode = GasAnalysisMode.Concluded;
                gs = gasEvent.reading();
            }
        }
    }
}
