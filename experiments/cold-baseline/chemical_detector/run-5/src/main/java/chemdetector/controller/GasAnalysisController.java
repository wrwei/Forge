package chemdetector.controller;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Intensity;
import chemdetector.data.Status;
import chemdetector.event.GAInputEvent;
import chemdetector.event.SharedEvent;
import chemdetector.mode.GAMode;
import chemdetector.sensor.Sensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem controller (CD-GA-FR1..CD-GA-FR4 + CD-GA-Beh1..7).
 *
 * Modes: Reading (initial) -> Analysis -> {NoGas, GasDetected} -> {Reading, Final}.
 * Emits the shared turn / stop / resume events to the movement controller.
 */
public final class GasAnalysisController {

    private GAMode currentMode = GAMode.Reading;

    /** CD-GA-Var1 — most recent gas reading. */
    private List<GasSensor> gs = new ArrayList<>();
    /** CD-GA-Var2 — outcome of the last analysis. */
    private Status sts = Status.noGas;
    /** CD-GA-Var3 — peak intensity of the most recent reading. */
    private Intensity ins = new Intensity(0.0);
    /** CD-GA-Var4 — angle of the strongest detected signal. */
    private Angle anl = Angle.Front;

    private final Sensor sensor;
    private final List<SharedEvent> outbox = new ArrayList<>();

    public GasAnalysisController(Sensor sensor) {
        this.sensor = sensor;
    }

    public GAMode currentMode() {
        return currentMode;
    }

    /** Returns and clears the buffered shared events emitted in this step. */
    public List<SharedEvent> drainOutbox() {
        List<SharedEvent> out = new ArrayList<>(outbox);
        outbox.clear();
        return out;
    }

    public void step(GAInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean peakAboveThr = Intensity.goreq(ins, Constants.thr);

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == GAMode.Reading) {
            // CD-GA-Beh2 — Reading -> Analysis on gas ? gs
            if (event instanceof GAInputEvent.Gas) {
                GAInputEvent.Gas ge = (GAInputEvent.Gas) event;
                gs = ge.gs();
                sensor.setReading(gs);
                currentMode = GAMode.Analysis;
                // Entry action of Analysis: sts = analysis(gs)
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GAMode.Analysis) {
            // CD-GA-Beh4 — Analysis -> NoGas when sts == noGas; action: send resume
            if (stsIsNoGas) {
                outbox.add(new SharedEvent.Resume());
                currentMode = GAMode.NoGas;
            }
            // CD-GA-Beh5 — Analysis -> GasDetected when sts == gasD
            else if (stsIsGasD) {
                currentMode = GAMode.GasDetected;
                // Entry action of GasDetected: ins = intensity(gs)
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GAMode.NoGas) {
            // CD-GA-Beh3 — NoGas -> Reading (autonomous, no guard)
            currentMode = GAMode.Reading;

        } else if (currentMode == GAMode.GasDetected) {
            // CD-GA-Beh6 — GasDetected -> Final when goreq(ins, thr); action: send stop
            if (peakAboveThr) {
                outbox.add(new SharedEvent.Stop());
                currentMode = GAMode.Final;
            }
            // CD-GA-Beh7 — GasDetected -> Reading when NOT goreq(ins, thr); send turn ! location(gs)
            else if (!peakAboveThr) {
                anl = sensor.location(gs);
                outbox.add(new SharedEvent.Turn(anl));
                currentMode = GAMode.Reading;
            }

        } else if (currentMode == GAMode.Final) {
            // Terminal — no outgoing transitions.
        }
    }

    @RoboChartType("real")
    public double currentIns() {
        return ins.value();
    }
}
