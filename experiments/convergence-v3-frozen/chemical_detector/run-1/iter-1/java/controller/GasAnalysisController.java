package chemical_detector.controller;

import chemical_detector.actuator.MovementBus;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemicalDetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import chemical_detector.event.GasAnalysisEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasAnalysisFunctions;
import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-ARCH2): it classifies the most recent gas
 * reading and decides whether to keep searching, to steer towards a stronger
 * signal, or to halt because the chemical source has been found.
 */
public final class GasAnalysisController {

    private final GasAnalysisFunctions functions;

    private final MovementBus bus;

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private List<GasSensor> gs = new ArrayList<GasSensor>();

    private Status sts = Status.noGas;

    @RoboChartType("real")
    private double ins = 0.0;

    private Angle anl = Angle.Front;

    public GasAnalysisController(GasAnalysisFunctions functions, MovementBus bus) {
        this.functions = functions;
        this.bus = bus;
    }

    public GasAnalysisMode currentMode() {
        return this.currentMode;
    }

    public void step(GasAnalysisEvent event) {
        boolean statusNoGas = this.sts == Status.noGas;
        boolean insAtOrAboveThr = functions.goreq(this.ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.gs();
                this.currentMode = GasAnalysisMode.Analysis;
                this.sts = functions.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (statusNoGas) {
                this.currentMode = GasAnalysisMode.NoGas;
                bus.resume();
            } else if (!statusNoGas) {
                this.currentMode = GasAnalysisMode.GasDetected;
                this.ins = functions.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            this.currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                this.currentMode = GasAnalysisMode.Stopped;
                bus.stop();
            } else if (!insAtOrAboveThr) {
                this.currentMode = GasAnalysisMode.Reading;
                this.anl = functions.location(this.gs);
                bus.turn(this.anl);
            }

        } else if (currentMode == GasAnalysisMode.Stopped) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.gs();
                this.currentMode = GasAnalysisMode.Stopped;
            }
        }
    }
}
