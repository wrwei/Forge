package chemdetector.controller;

import java.util.List;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.VehicleSensors;

/**
 * Gas-analysis subsystem: classifies sensor readings and decides
 * whether to keep searching (resume), steer towards a stronger signal
 * (turn), or stop because the chemical source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    private final VehicleSensors sensors;
    private final Actuator actuator;

    public GasAnalysisController(VehicleSensors sensors, Actuator actuator) {
        this.sensors = sensors;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean insAtOrAboveThr = sensors.goreq(ins, Constants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                this.gs = g.payload();
                currentMode = GasAnalysisMode.Analysis;
                this.sts = sensors.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                this.ins = sensors.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Done;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                this.anl = sensors.location(this.gs);
                actuator.apply(new OutputEvent.Turn(this.anl));
            }

        } else if (currentMode == GasAnalysisMode.Done) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                this.gs = g.payload();
                currentMode = GasAnalysisMode.Done;
            }
        }
    }
}
