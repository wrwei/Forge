package chemical_detector.controller;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.GasAnalysisEvent;
import chemical_detector.event.MovementBus;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasFunctions;

import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer towards the strongest signal
 * (turn) or halt because the source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;

    private final GasFunctions functions;
    private final MovementBus bus;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasFunctions functions, MovementBus bus) {
        this.functions = functions;
        this.bus = bus;
    }

    /** Executes one control cycle, handling {@code event} and any autonomous transition. */
    public void step(GasAnalysisEvent event) {
        boolean readingHasNoGas = this.sts == Status.noGas;
        boolean peakAtOrAboveThr = this.ins >= ChemConstants.THR;

        if (this.currentMode == GasAnalysisMode.READING) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas gas = (GasAnalysisEvent.Gas) event;
                this.gs = gas.reading();
                this.currentMode = GasAnalysisMode.ANALYSIS;
                this.sts = this.functions.analysis(this.gs);
            }
        } else if (this.currentMode == GasAnalysisMode.ANALYSIS) {
            if (readingHasNoGas) {
                this.currentMode = GasAnalysisMode.NO_GAS;
                this.bus.resume();
            } else {
                this.currentMode = GasAnalysisMode.GAS_DETECTED;
                this.ins = this.functions.intensity(this.gs);
            }
        } else if (this.currentMode == GasAnalysisMode.NO_GAS) {
            this.currentMode = GasAnalysisMode.READING;
        } else if (this.currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (peakAtOrAboveThr) {
                this.currentMode = GasAnalysisMode.SOURCE_FOUND;
                this.bus.stop();
            } else {
                this.currentMode = GasAnalysisMode.READING;
                this.anl = this.functions.location(this.gs);
                this.bus.turn(this.anl);
            }
        } else if (this.currentMode == GasAnalysisMode.SOURCE_FOUND) {
            this.currentMode = GasAnalysisMode.CONCLUDED;
        } else if (this.currentMode == GasAnalysisMode.CONCLUDED) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas gas = (GasAnalysisEvent.Gas) event;
                this.gs = gas.reading();
                this.currentMode = GasAnalysisMode.CONCLUDED;
            }
        }
    }

    public GasAnalysisMode currentMode() {
        return this.currentMode;
    }

    public List<GasSensor> gs() {
        return this.gs;
    }

    public Status sts() {
        return this.sts;
    }

    @RoboChartType("real")
    public double ins() {
        return this.ins;
    }

    public Angle anl() {
        return this.anl;
    }
}
