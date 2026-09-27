package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorArray;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem
 * to keep searching (resume), steer towards the strongest signal (turn) or halt (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensorArray gasSensors;
    private final Actuator actuator;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorArray gasSensors, Actuator actuator) {
        this.gasSensors = gasSensors;
        this.actuator = actuator;
    }

    /**
     * Runs one control cycle.
     *
     * @param event the input received this cycle, or {@code null} when none arrived
     *              (only autonomous transitions can then fire)
     */
    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = gasSensors.goreq(ins, DetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                gs = gasEvent.readings();
                sts = gasSensors.analysis(gs);
                currentMode = GasAnalysisMode.Analysis;
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                actuator.apply(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (!stsIsNoGas) {
                ins = gasSensors.intensity(gs);
                currentMode = GasAnalysisMode.GasDetected;
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                actuator.apply(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Concluded;
            } else if (!insAtOrAboveThr) {
                anl = gasSensors.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                gs = gasEvent.readings();
                currentMode = GasAnalysisMode.Concluded;
            }
        }
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public List<GasSensor> gs() {
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
}
