package chemdetector.controller;

import java.util.ArrayList;
import java.util.List;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.GasAnalyzer;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and decides
 * whether to keep searching (resume), steer towards a stronger signal (turn),
 * or stop because the chemical source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    /** Most recent gas reading (CD-GA-Var1). */
    private List<GasSensor> gs = new ArrayList<>();
    /** Outcome of the most recent analysis (CD-GA-Var2). */
    private Status sts = Status.noGas;
    /** Peak intensity of the most recent reading (CD-GA-Var3). */
    @RoboChartType("real")
    private double ins;
    /** Direction of the strongest detected signal (CD-GA-Var4). */
    private Angle anl = Angle.Front;

    private final GasAnalyzer analyzer;
    private final Actuator actuator;

    public GasAnalysisController(GasAnalyzer analyzer, Actuator actuator) {
        this.analyzer = analyzer;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsIsGasD = sts == Status.gasD;
        boolean insAtOrAboveThr = ins >= ChemConstants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.value();
                currentMode = GasAnalysisMode.Analysis;
                sts = analyzer.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = analyzer.intensity(gs);
            } else if (!stsIsGasD) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Done;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                anl = analyzer.location(gs);
                currentMode = GasAnalysisMode.Reading;
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Done) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.value();
                currentMode = GasAnalysisMode.Done;
            }
        }
    }
}
