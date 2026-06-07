package chemdetector.controller;

import java.util.List;

import chemdetector.actuator.SignalBus;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.Sensor;

/**
 * Gas-analysis subsystem: classifies each gas reading and decides whether
 * to keep searching, steer toward the strongest signal, or declare the
 * chemical source found.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final Sensor sensor;
    private final SignalBus bus;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, SignalBus bus) {
        this.sensor = sensor;
        this.bus = bus;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public void step(GasAnalysisEvent event) {
        boolean stsIsNoGas = this.sts == Status.noGas;
        boolean stsIsGasD = this.sts == Status.gasD;
        boolean insAtOrAboveThr = this.ins >= ChemConstants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                this.sts = sensor.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                bus.resume();
            } else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                this.ins = sensor.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                bus.stop();
                currentMode = GasAnalysisMode.Finished;
            } else if (!insAtOrAboveThr) {
                this.anl = sensor.location(this.gs);
                bus.turn(this.anl);
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Finished) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.reading();
                currentMode = GasAnalysisMode.Finished;
            }
        }
    }
}
