package chemical_detector.controller;

import chemical_detector.actuator.Actuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer toward the strongest signal (turn), or halt
 * because the source has been found (stop).
 */
public final class GasAnalysisController {

    private final Sensor sensor;
    private final Actuator actuator;

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** Runs one control cycle, consuming {@code event} if the current state accepts it. */
    public void step(InputEvent event) {
        boolean stsNoGas = sts == Status.noGas;
        boolean insAtOrAboveThr = sensor.goreq(ins, DetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas reading = (InputEvent.gas) event;
                gs = reading.readings();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.resume());
            } else if (!stsNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.stop());
            } else if (!insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.gas) {
                InputEvent.gas reading = (InputEvent.gas) event;
                gs = reading.readings();
                currentMode = GasAnalysisMode.Concluded;
            }
        }
    }
}
