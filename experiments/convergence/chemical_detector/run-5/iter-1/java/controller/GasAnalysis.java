package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GaMode;
import chemdetector.sensor.Telemetry;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies sensor readings and decides
 * whether to keep searching, steer towards a stronger signal, or stop
 * because the chemical source has been found.
 */
public final class GasAnalysis {

    private GaMode currentMode = GaMode.Reading;
    private final Telemetry telemetry;
    private final Actuator actuator;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins;
    private Angle anl = Angle.Front;

    public GasAnalysis(Telemetry telemetry, Actuator actuator) {
        this.telemetry = telemetry;
        this.actuator = actuator;
    }

    public GaMode currentMode() {
        return currentMode;
    }

    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = ins >= ChemConstants.thr;

        if (currentMode == GaMode.Reading) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas g = (InputEvent.gas) event;
                gs = g.value();
                currentMode = GaMode.Analysis;
                sts = telemetry.analysis(gs);
            }

        } else if (currentMode == GaMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GaMode.NoGas;
                actuator.apply(new OutputEvent.resume());
            } else if (!stsIsNoGas) {
                currentMode = GaMode.GasDetected;
                ins = telemetry.intensity(gs);
            }

        } else if (currentMode == GaMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GaMode.Done;
                actuator.apply(new OutputEvent.stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GaMode.Reading;
                anl = telemetry.location(gs);
                actuator.apply(new OutputEvent.turn(anl));
            }

        } else if (currentMode == GaMode.NoGas) {
            currentMode = GaMode.Reading;

        } else if (currentMode == GaMode.Done) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas g = (InputEvent.gas) event;
                gs = g.value();
                currentMode = GaMode.Done;
            }
        }
    }
}
