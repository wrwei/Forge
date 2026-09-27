package chemical_detector.controller;

import chemical_detector.actuator.Emitter;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import chemical_detector.event.SystemEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.VehicleSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem. Classifies the most recent reading and decides
 * whether the movement subsystem should keep searching, steer towards a
 * stronger signal, or halt because the chemical source has been found.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final VehicleSensor sensor;

    private final Emitter emitter;

    private List<GasSensor> gs = new ArrayList<>();

    private Status sts = Status.noGas;

    @RoboChartType("real")
    private double ins = 0.0;

    private Angle anl = Angle.Front;

    public GasAnalysisController(VehicleSensor sensor, Emitter emitter) {
        this.sensor = sensor;
        this.emitter = emitter;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
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

    public List<GasSensor> gs() {
        return gs;
    }

    /** One control cycle: evaluates every transition leaving the current mode. */
    public void step(SystemEvent event) {
        // --- Named boolean predicates ---
        boolean stsNoGas = this.sts == Status.noGas;
        boolean stsGasDetected = this.sts == Status.gasD;
        boolean insAtOrAboveThr = this.ins >= ChemConstants.THR;

        // --- Mode-nested if-else ---
        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof SystemEvent.Gas) {
                SystemEvent.Gas g = (SystemEvent.Gas) event;
                this.gs = g.gs();
                currentMode = GasAnalysisMode.Analysis;
                this.sts = sensor.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                emitter.resume();
            } else if (stsGasDetected) {
                currentMode = GasAnalysisMode.GasDetected;
                this.ins = sensor.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Stopped;
                emitter.stop();
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                this.anl = sensor.location(this.gs);
                emitter.apply(new SystemEvent.Turn(this.anl));
            }

        } else if (currentMode == GasAnalysisMode.Stopped) {
            if (event instanceof SystemEvent.Tick) {
                currentMode = GasAnalysisMode.Stopped;
            }
        }
    }
}
