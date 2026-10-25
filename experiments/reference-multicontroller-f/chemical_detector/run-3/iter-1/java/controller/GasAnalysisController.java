package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.Constants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.ChemSensor;
import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem to
 * resume searching, turn towards a stronger signal, or stop because the source is found.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final ChemSensor sensor;
    private final Actuator actuator;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins;
    private Angle anl = Angle.Front;

    public GasAnalysisController(ChemSensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** Runs one control cycle: at most one transition, event-triggered or autonomous. */
    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = sensor.goreq(ins, Constants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasEvent = (InputEvent.Gas) event;
                this.gs = gasEvent.readings();
                currentMode = GasAnalysisMode.Analysis;
                this.sts = sensor.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            } else if (!stsIsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                this.ins = sensor.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                this.anl = sensor.location(this.gs);
                actuator.apply(new OutputEvent.Turn(this.anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.Tick) {
                currentMode = GasAnalysisMode.Concluded;
            }
        }
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
}
