package chemical_detector.controller;

import chemical_detector.DetectorConstants;
import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-ARCH2): classifies each gas reading and
 * tells the movement subsystem to keep searching (resume), to steer towards
 * the strongest signal (turn), or that the source is found (stop).
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    // CD-GA-Var1: the most recent gas reading, initially empty.
    private List<GasSample> gs = new ArrayList<>();
    // CD-GA-Var2: classification of gs, set on entry to Analysis.
    private Status sts = Status.noGas;
    // CD-GA-Var3: peak intensity of gs, set on entry to GasDetected.
    @RoboChartType("real") private double ins = 0.0;
    // CD-GA-Var4: direction of the strongest signal, sent over turn.
    private Angle anl = Angle.Front;

    private final ChemicalSensor sensor;
    private final GasAnalysisActuator actuator;

    /** Creates the controller in Reading (CD-GA-Beh1). */
    public GasAnalysisController(ChemicalSensor sensor, GasAnalysisActuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** One control cycle: takes at most one transition. */
    public void step(InputEvent event) {
        boolean readingHasNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThr = sensor.goreq(ins, DetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: adopt the carried reading as gs, then Analysis entry sts = analysis(gs).
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasSignal = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = gasSignal.reading();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: noGas, send resume on the way into NoGas.
            if (readingHasNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                actuator.apply(new OutputEvent.Resume());
            }
            // CD-GA-Beh5: gasD, then GasDetected entry ins = intensity(gs).
            else if (!readingHasNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: unconditional return to Reading.
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: peak at or above thr, send stop and conclude.
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                actuator.apply(new OutputEvent.Stop());
            }
            // CD-GA-Beh7: peak below thr, turn towards the strongest signal.
            else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                actuator.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Terminal: the search has concluded; no further readings are processed.
        }
    }
}
