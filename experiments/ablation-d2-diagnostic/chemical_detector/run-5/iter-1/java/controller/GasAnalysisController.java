package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemicalDetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorFunctions;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem
 * to keep searching (resume), to steer toward the strongest signal (turn), or that the
 * source has been found (stop). Once the source is found it retains later readings but
 * analyses none of them.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private List<GasSample> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final GasSensorFunctions analyser;
    private final Actuator actuator;

    public GasAnalysisController(GasSensorFunctions analyser, Actuator actuator) {
        this.analyser = analyser;
        this.actuator = actuator;
    }

    /**
     * Performs one control cycle. {@code event} is the event received in this cycle,
     * or {@code null} if none; autonomous transitions fire on any cycle.
     */
    public void step(InputEvent event) {
        boolean stsGasD = sts == Status.gasD;
        boolean insAtOrAboveThr = analyser.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gas = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gas.gs();
                sts = analyser.analysis(gs);
            }
        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = analyser.intensity(gs);
            } else if (!stsGasD) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }
        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;
        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = analyser.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }
        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gas = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Concluded;
                gs = gas.gs();
            }
        }
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }
}
