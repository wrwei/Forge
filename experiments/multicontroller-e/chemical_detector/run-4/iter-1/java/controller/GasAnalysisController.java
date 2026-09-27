package chemical_detector.controller;

import chemical_detector.actuator.OutputPort;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer towards the signal (turn) or halt
 * because the source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final Sensor sensor;
    private final OutputPort output;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, OutputPort output) {
        this.sensor = sensor;
        this.output = output;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs at most one transition. Pass {@code null} to evaluate only the
     * autonomous (event-free) transitions.
     */
    public void step(InputEvent event) {
        boolean stsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = sensor.goreq(ins, Constants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gas = (InputEvent.Gas) event;
                gs = gas.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsNoGas) {
                output.send(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (!stsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                output.send(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Concluded;
            } else if (!insAtOrAboveThr) {
                anl = sensor.location(gs);
                output.send(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // The search is over: later readings are accepted but not analysed.
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gas = (InputEvent.Gas) event;
                gs = gas.reading();
                currentMode = GasAnalysisMode.Concluded;
            }
        }
    }
}
