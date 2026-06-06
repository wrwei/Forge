package chemdetector.controller;

import chemdetector.actuator.GasAnalysisOutput;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.DetectorConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.mode.GasMode;
import chemdetector.sensor.DetectorSensors;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies sensor readings and
 * decides whether to keep searching, steer towards a stronger signal, or
 * stop because the chemical source has been found.
 */
public final class GasAnalysisController {

    private GasMode currentMode = GasMode.Reading;
    private final DetectorSensors sensor;
    private final GasAnalysisOutput output;

    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins;
    private Angle anl = Angle.Front;

    public GasAnalysisController(DetectorSensors sensor, GasAnalysisOutput output) {
        this.sensor = sensor;
        this.output = output;
    }

    public GasMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean statusNoGas = sts == Status.noGas;
        boolean intensityAtThreshold = ins >= DetectorConstants.THR;

        if (currentMode == GasMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas g = (InputEvent.Gas) event;
                gs = g.reading();
                currentMode = GasMode.Analysis;
                sts = sensor.analysis(gs);
            }
        } else if (currentMode == GasMode.Analysis) {
            if (statusNoGas) {
                output.apply(new InputEvent.Resume());
                currentMode = GasMode.NoGas;
            } else if (!statusNoGas) {
                currentMode = GasMode.GasDetected;
                ins = sensor.intensity(gs);
            }
        } else if (currentMode == GasMode.NoGas) {
            currentMode = GasMode.Reading;
        } else if (currentMode == GasMode.GasDetected) {
            if (intensityAtThreshold) {
                output.apply(new InputEvent.Stop());
                currentMode = GasMode.Reading;
            } else if (!intensityAtThreshold) {
                anl = sensor.location(gs);
                output.apply(new InputEvent.Turn(anl));
                currentMode = GasMode.Reading;
            }
        }
    }
}
