package chemdetector.controller;

import chemdetector.actuator.Vehicle;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.DetectorConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.GasSensorArray;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies sensor readings and decides
 * whether to keep searching (resume), steer towards a stronger signal
 * (turn), or stop because the chemical source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasSensorArray sensor;
    private final Vehicle vehicle;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;

    @RoboChartType("real")
    private double ins;

    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorArray sensor, Vehicle vehicle) {
        this.sensor = sensor;
        this.vehicle = vehicle;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsNoGas = sts == Status.noGas;
        boolean stsGasD = sts == Status.gasD;
        boolean insAboveThr = ins >= DetectorConstants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            } else if (stsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                vehicle.apply(new OutputEvent.Resume());
            } else if (stsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            } else if (insAboveThr) {
                currentMode = GasAnalysisMode.Located;
                vehicle.apply(new OutputEvent.Stop());
            } else if (!insAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                vehicle.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Located) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasAnalysisMode.Located;
            }
        }
    }
}
