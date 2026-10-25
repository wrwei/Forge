package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
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
 * subsystem to keep searching (resume), steer towards the strongest signal
 * (turn) or stop because the chemical source has been found (stop).
 */
public final class GasAnalysis {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final Sensor sensor;
    private final Actuator actuator;

    private List<GasSensor> gs;
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysis(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.gs = List.of();
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

    /** Runs one control cycle of the gas-analysis subsystem on {@code event}. */
    public void step(InputEvent event) {
        boolean stsNoGas = this.sts == Status.noGas;
        boolean insAtOrAboveThr = sensor.goreq(this.ins, Constants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gas = (InputEvent.Gas) event;
                this.gs = gas.reading();
                currentMode = GasAnalysisMode.ANALYSIS;
                this.sts = sensor.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            if (stsNoGas) {
                actuator.apply(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NO_GAS;
            } else if (!stsNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                this.ins = sensor.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (insAtOrAboveThr) {
                actuator.apply(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.CONCLUDED;
            } else if (!insAtOrAboveThr) {
                this.anl = sensor.location(this.gs);
                actuator.apply(new OutputEvent.Turn(this.anl));
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            if (event instanceof InputEvent.Tick) {
                currentMode = GasAnalysisMode.CONCLUDED;
            }
        }
    }
}
