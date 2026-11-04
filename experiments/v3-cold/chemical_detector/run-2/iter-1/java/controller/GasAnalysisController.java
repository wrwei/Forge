package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.EventBus;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasSensorArray;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), steer towards the strongest signal
 * (turn) or halt because the source has been found (stop).
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasSensorArray sensor;
    private final EventBus bus;

    private List<GasSample> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    /**
     * Creates the subsystem in its initial mode, Reading.
     *
     * @param sensor the gas-sensor array that classifies readings
     * @param bus where turn, stop and resume are sent
     */
    public GasAnalysisController(GasSensorArray sensor, EventBus bus) {
        this.sensor = sensor;
        this.bus = bus;
    }

    /**
     * Performs one control cycle.
     *
     * @param event the event received in this cycle, or null if none
     */
    public void step(InputEvent event) {
        boolean statusNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThr = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasReading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSING;
                gs = gasReading.readings();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSING) {
            if (statusNoGas) {
                currentMode = GasAnalysisMode.GAS_ABSENT;
                bus.apply(new OutputEvent.Resume());
            } else if (!statusNoGas) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.GAS_ABSENT) {
            // Autonomous, unguarded: go back for the next reading.
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.CONCLUDED;
                bus.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.READING;
                anl = sensor.location(gs);
                bus.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Terminal: the source has been found; no further readings.
        }
    }
}
