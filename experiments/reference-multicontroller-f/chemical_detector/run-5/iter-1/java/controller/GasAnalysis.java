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
import chemical_detector.sensor.VehicleSensors;
import java.util.List;

/** Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem what to do. */
public final class GasAnalysis {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;

    private final VehicleSensors sensors;

    private final Actuator actuator;

    private List<GasSensor> gs = List.of();

    private Status sts = Status.noGas;

    @RoboChartType("real")
    private double ins = 0.0;

    private Angle anl = Angle.Front;

    public GasAnalysis(VehicleSensors sensors, Actuator actuator) {
        this.sensors = sensors;
        this.actuator = actuator;
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

    /** Performs at most one transition of the gas-analysis state machine. */
    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = ins >= DetectorConstants.THR;

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                gs = gasEvent.readings();
                currentMode = GasAnalysisMode.ANALYSIS;
                sts = sensors.analysis(gs);
            }
        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NO_GAS;
                actuator.apply(new OutputEvent.Resume());
            } else if (!stsIsNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensors.intensity(gs);
            }
        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;
        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.CONCLUDED;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.READING;
                anl = sensors.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }
        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            if (event instanceof InputEvent.Tick) {
                currentMode = GasAnalysisMode.CONCLUDED;
            }
        }
    }
}
