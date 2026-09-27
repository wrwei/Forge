package chemical_detector.controller;

import java.util.List;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasAnalysisFunctions;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), head for the strongest signal (turn)
 * or stop because the source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasAnalysisFunctions functions;
    private final MovementCommands movementCommands;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasAnalysisFunctions functions, MovementCommands movementCommands) {
        this.functions = functions;
        this.movementCommands = movementCommands;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Most recent gas reading (initially empty). */
    public List<GasSensor> gs() {
        return gs;
    }

    /** Classification of the most recent reading. */
    public Status sts() {
        return sts;
    }

    /** Peak intensity of the most recent gas-detected reading. */
    @RoboChartType("real")
    public double ins() {
        return ins;
    }

    /** Direction of the strongest signal last sent over turn. */
    public Angle anl() {
        return anl;
    }

    /** Performs one control cycle. */
    public void step(InputEvent event) {
        boolean stsNoGas = this.sts == Status.noGas;
        boolean stsGasD = this.sts == Status.gasD;
        boolean insAtOrAboveThr = this.ins >= ChemConstants.THR;

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                this.gs = g.gs();
                currentMode = GasAnalysisMode.ANALYSIS;
                this.sts = functions.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            if (stsNoGas) {
                movementCommands.resume();
                currentMode = GasAnalysisMode.NO_GAS;
            } else if (stsGasD) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                this.ins = functions.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (insAtOrAboveThr) {
                movementCommands.stop();
                currentMode = GasAnalysisMode.FINAL;
            } else {
                this.anl = functions.location(this.gs);
                movementCommands.turn(this.anl);
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.FINAL) {
            if (event instanceof InputEvent.Tick) {
                currentMode = GasAnalysisMode.FINAL;
            }
        }
    }
}
