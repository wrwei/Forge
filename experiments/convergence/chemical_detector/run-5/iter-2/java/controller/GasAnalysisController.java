package chemdetector.controller;

import java.util.List;

import chemdetector.actuator.SignalPort;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.ChemSensorService;

/**
 * Gas-analysis subsystem: classifies sensor readings and decides
 * whether to keep searching, steer towards a stronger signal, or stop
 * because the chemical source has been found (CD-ARCH2).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final ChemSensorService sensor;
    private final SignalPort signals = new SignalPort();

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins;
    private Angle anl = Angle.Front;

    public GasAnalysisController(ChemSensorService sensor) {
        this.sensor = sensor;
    }

    public void step(GasAnalysisEvent event) {
        boolean stsIsNoGas = this.sts == Status.noGas;
        boolean insAtOrAboveThr = this.ins >= ChemConstants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.value();
                this.sts = sensor.analysis(this.gs);
                currentMode = GasAnalysisMode.Analysis;
            }
        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                signals.send(new MovementEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (!stsIsNoGas) {
                this.ins = sensor.intensity(this.gs);
                currentMode = GasAnalysisMode.GasDetected;
            }
        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;
        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                signals.send(new MovementEvent.Stop());
                currentMode = GasAnalysisMode.Reading;
            } else if (!insAtOrAboveThr) {
                this.anl = sensor.location(this.gs);
                signals.send(new MovementEvent.Turn(this.anl));
                currentMode = GasAnalysisMode.Reading;
            }
        }
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public SignalPort signals() {
        return signals;
    }
}
