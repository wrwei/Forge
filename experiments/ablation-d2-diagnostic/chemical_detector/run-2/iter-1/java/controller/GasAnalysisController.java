package chemical_detector.controller;

import chemical_detector.actuator.EventActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorArray;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching ({@code resume}), steer towards a stronger signal
 * ({@code turn}) or halt because the source is found ({@code stop}).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensorArray gasSensors;
    private final EventActuator actuator;

    private List<GasSample> gs;
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorArray gasSensors, EventActuator actuator) {
        this.gasSensors = gasSensors;
        this.actuator = actuator;
        this.gs = List.of();
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public List<GasSample> gs() {
        return gs;
    }

    public Status sts() {
        return sts;
    }

    @RoboChartType("real")
    public double ins() {
        return ins;
    }

    public Angle anl() {
        return anl;
    }

    /**
     * Performs one control cycle. {@code event} is the input received this cycle,
     * or {@code null} when none arrived.
     */
    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = ins >= ChemConstants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas gasEvent = (InputEvent.gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasEvent.reading();
                sts = gasSensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.resume());
            } else if (!stsIsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = gasSensors.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = gasSensors.location(gs);
                actuator.apply(new OutputEvent.turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas gasEvent = (InputEvent.gas) event;
                currentMode = GasAnalysisMode.Concluded;
                gs = gasEvent.reading();
            }
        }
    }
}
