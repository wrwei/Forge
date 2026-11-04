package chemical_detector.controller;

import chemical_detector.actuator.SignalActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorArray;

import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the
 * movement subsystem to resume searching, turn towards the strongest
 * signal, or stop because the source is found.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasSensorArray sensor;
    private final SignalActuator signals;

    private List<GasSensor> gs;
    private Status sts = Status.noGas;
    @RoboChartType("real") private double ins = 0.0;
    private Angle anl = Angle.Front;

    /** Creates the subsystem in Reading with an empty last reading. */
    public GasAnalysisController(GasSensorArray sensor, SignalActuator signals) {
        this.sensor = sensor;
        this.signals = signals;
        this.gs = List.of();
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Performs one control cycle. {@code event} is the event received in
     * this cycle, or {@code null} if none; transitions without a trigger
     * fire on any call.
     */
    public void step(InputEvent event) {
        boolean readingHasTarget = sts == Status.gasD;
        boolean peakAtOrAboveThr = sensor.goreq(ins, DetectorConstants.THR);

        if (currentMode == GasAnalysisMode.READING) {
            // CD-GA-Beh2: adopt the reading and classify it on entry to Analysis.
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasReading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.ANALYSIS;
                gs = gasReading.reading();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            // CD-GA-Beh5: compute the peak intensity on entry to GasDetected.
            if (readingHasTarget) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                ins = sensor.intensity(gs);
            }
            // CD-GA-Beh4
            else if (!readingHasTarget) {
                currentMode = GasAnalysisMode.NO_GAS;
                signals.apply(new OutputEvent.Resume());
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            // CD-GA-Beh3: unguarded return to Reading.
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            // CD-GA-Beh6
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.CONCLUDED;
                signals.apply(new OutputEvent.Stop());
            }
            // CD-GA-Beh7
            else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.READING;
                anl = sensor.location(gs);
                signals.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            // Terminal: the search is over and no further readings are processed.
        }
    }
}
