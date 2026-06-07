package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.sensor.GasSensors;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and decides whether to
 * keep searching, steer toward a stronger signal, or declare the source found
 * (CD-ARCH2, CD-GA-FR1..4, CD-GA-Beh1..7).
 *
 * <p>The source-found outcome emits the stop event and returns to Reading
 * instead of entering a terminal state, so every mode stays live.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensors gasSensors;
    private final Actuator actuator;

    /** Most recent gas reading (CD-GA-Var1). */
    private List<GasSensor> gs = List.of();
    /** Outcome of the most recent analysis (CD-GA-Var2). */
    private Status sts = Status.noGas;
    /** Peak intensity of the most recent gasD reading (CD-GA-Var3). */
    @RoboChartType("real")
    private double ins = 0.0;
    /** Direction of the strongest detected signal (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensors gasSensors, Actuator actuator) {
        this.gasSensors = gasSensors;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsIsGasD = sts == Status.gasD;
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = ins >= Constants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = gasSensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasSensors.intensity(gs);
            } else if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasSensors.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }
        }
    }
}
