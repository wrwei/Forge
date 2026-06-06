package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.GasFunctions;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies the most recent gas
 * reading and decides whether to keep searching (resume), home in on a
 * stronger signal (turn), or stop because the source is found (stop).
 *
 * <p>Single-method, mode-nested if-else state machine over
 * {@link GasAnalysisMode}. Reading is the initial state; Final is the
 * terminal state j1.</p>
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasFunctions functions;
    private final Actuator actuator;

    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasFunctions functions, Actuator actuator) {
        this.functions = functions;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = ins >= Constants.thr;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                sts = functions.analysis(gs);
                currentMode = GasAnalysisMode.Analysis;
            }
        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsNoGas) {
                actuator.apply(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else {
                ins = functions.intensity(gs);
                currentMode = GasAnalysisMode.GasDetected;
            }
        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;
        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                actuator.apply(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Final;
            } else {
                anl = functions.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }
        } else if (currentMode == GasAnalysisMode.Final) {
            if (event instanceof InputEvent.Tick) {
                currentMode = GasAnalysisMode.Final;
            }
        }
    }
}
