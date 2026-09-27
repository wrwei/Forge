package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.types.Angle;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement subsystem
 * to keep searching (resume), steer towards the strongest signal (turn), or halt (stop).
 */
public final class GasAnalysis {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final VehicleSensors sensors;

    private final Actuator actuator;

    private List<GasSensor> gs = List.of();

    private Status sts = Status.noGas;

    @RoboChartType("real")
    private double ins;

    private Angle anl = Angle.Front;

    public GasAnalysis(VehicleSensors sensors, Actuator actuator) {
        this.sensors = sensors;
        this.actuator = actuator;
    }

    /**
     * Performs one control cycle. {@code event} is the input received in this cycle,
     * or {@code null} when the cycle carries no input.
     */
    public void step(InputEvent event) {
        boolean stsIsNoGas = sts == Status.noGas;
        boolean insGoreqThr = sensors.goreq(ins, DetectorConstants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas reading = (InputEvent.gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = reading.readings();
                sts = sensors.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.resume());
            } else if (!stsIsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensors.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insGoreqThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.stop());
            } else if (!insGoreqThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensors.location(gs);
                actuator.apply(new OutputEvent.turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Search is over: later readings are retained but never classified.
            if (event instanceof InputEvent.gas) {
                InputEvent.gas reading = (InputEvent.gas) event;
                currentMode = GasAnalysisMode.Concluded;
                gs = reading.readings();
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
