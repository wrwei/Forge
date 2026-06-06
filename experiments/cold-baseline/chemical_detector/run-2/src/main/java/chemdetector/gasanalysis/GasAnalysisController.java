package chemdetector.gasanalysis;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Intensity;
import chemdetector.data.Status;
import chemdetector.gasanalysis.event.InputEvent;
import chemdetector.gasanalysis.event.OutputEvent;
import chemdetector.gasanalysis.mode.GAMode;
import chemdetector.sensor.GasFunctions;
import java.util.ArrayList;
import java.util.List;

/**
 * CD-ARCH2 gas-analysis subsystem. Single-method, mode-nested if-else
 * state machine over the modes in {@link GAMode}.
 *
 * <p>Modes: Reading -> Analysis -> {NoGas | GasDetected} -> Final/Reading.
 *
 * <p>Outputs (turn, stop, resume) are buffered on a per-step queue
 * that the movement subsystem polls. The Final mode is the sink (j1)
 * after stop has been issued.
 */
public final class GasAnalysisController {

    private final GasFunctions gasFunctions;
    private final List<OutputEvent> outbox = new ArrayList<>();

    private GAMode currentMode = GAMode.Reading;

    /** CD-GA-Var1: last gas reading received. */
    private List<GasSensor> gs = new ArrayList<>();

    /** CD-GA-Var2: classification of the last reading. */
    private Status sts = Status.noGas;

    /** CD-GA-Var3: peak intensity of the last reading. */
    private Intensity ins = new Intensity(0.0);

    /** CD-GA-Var4: angle of the strongest signal in the last reading. */
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasFunctions gasFunctions) {
        this.gasFunctions = gasFunctions;
    }

    public GAMode currentMode() {
        return currentMode;
    }

    public Status sts() {
        return sts;
    }

    public Intensity ins() {
        return ins;
    }

    public Angle anl() {
        return anl;
    }

    /** Drain the buffered outputs (turn / stop / resume). */
    public List<OutputEvent> drain() {
        List<OutputEvent> snapshot = new ArrayList<>(outbox);
        outbox.clear();
        return snapshot;
    }

    /**
     * Single-method, mode-nested if-else state machine. One step
     * evaluates exactly one transition: the event-triggered one if
     * the event matches; otherwise the autonomous guarded transition
     * for the current mode.
     */
    public void step(InputEvent event) {
        // Named boolean predicates declared BEFORE the if-else chain.
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean intensityAboveThr = gasFunctions.goreq(ins, Constants.thr);
        boolean intensityBelowThr = !gasFunctions.goreq(ins, Constants.thr);

        if (currentMode == GAMode.Reading) {
            // CD-GA-Beh2: on Gas event, store gs, classify, go to Analysis.
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas ge = (InputEvent.Gas) event;
                gs = ge.gs();
                sts = gasFunctions.analysis(gs);
                currentMode = GAMode.Analysis;
            }
        } else if (currentMode == GAMode.Analysis) {
            // CD-GA-Beh4: sts == noGas -> NoGas, send resume.
            if (stsIsNoGas) {
                outbox.add(new OutputEvent.Resume());
                currentMode = GAMode.NoGas;
            }
            // CD-GA-Beh5: sts == gasD -> GasDetected, compute ins.
            else if (stsIsGasD) {
                ins = gasFunctions.intensity(gs);
                currentMode = GAMode.GasDetected;
            }
        } else if (currentMode == GAMode.NoGas) {
            // CD-GA-Beh3: autonomous transition back to Reading.
            currentMode = GAMode.Reading;
        } else if (currentMode == GAMode.GasDetected) {
            // CD-GA-Beh6: ins >= thr -> Final, send stop.
            if (intensityAboveThr) {
                outbox.add(new OutputEvent.Stop());
                currentMode = GAMode.Final;
            }
            // CD-GA-Beh7: ins < thr -> Reading, send turn ! anl.
            else if (intensityBelowThr) {
                anl = gasFunctions.location(gs);
                outbox.add(new OutputEvent.Turn(anl));
                currentMode = GAMode.Reading;
            }
        }
        // Final is a sink (j1) — no outgoing transitions.
    }

    /** Lift method needed by Spoon's natural-number inference. */
    @RoboChartType("nat")
    public int gasCount() {
        return gs.size();
    }
}
