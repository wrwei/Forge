package chemdetector.controller;

import chemdetector.constants.Constants;
import chemdetector.controller.mode.GasAnalysisMode;
import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Intensity;
import chemdetector.domain.Status;
import chemdetector.event.GasAnalysisInputEvent;
import chemdetector.sensor.Sensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem (CD-GA-FR1..FR4 + CD-GA-Beh1..7).
 *
 * <p>Single-method, mode-nested if-else state machine. Reading is the initial
 * state. Analysis assigns sts on entry, GasDetected assigns ins on entry. Both
 * autonomous branches out of Analysis and GasDetected are decided by named
 * boolean predicates evaluated before the outer if-else.</p>
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final Sensor sensor;
    private final MovementController movement;

    private List<GasSensor> gs = new ArrayList<GasSensor>();
    private Status sts = Status.noGas;
    @chemdetector.annotation.RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, MovementController movement) {
        this.sensor = sensor;
        this.movement = movement;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public Status sts() {
        return sts;
    }

    @chemdetector.annotation.RoboChartType("real")
    public double ins() {
        return ins;
    }

    public Angle anl() {
        return anl;
    }

    public void step(GasAnalysisInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the outer if-else) ---
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean intensityAboveThr = Intensity.goreq(new Intensity(ins), Constants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: gas ? gs -> Analysis
            if (event instanceof GasAnalysisInputEvent.Gas) {
                GasAnalysisInputEvent.Gas g = (GasAnalysisInputEvent.Gas) event;
                this.gs = g.gs();
                currentMode = GasAnalysisMode.Analysis;
                // Analysis entry action (CD-GA-FR3): sts = analysis(gs)
                this.sts = sensor.analysis(this.gs);
            }
        } else if (currentMode == GasAnalysisMode.Analysis) {
            // Autonomous; guards are stsIsNoGas / stsIsGasD.
            if (stsIsNoGas) {
                // CD-GA-Beh4: -> NoGas, send resume
                movement.receive(new chemdetector.event.MovementInputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (stsIsGasD) {
                // CD-GA-Beh5: -> GasDetected
                currentMode = GasAnalysisMode.GasDetected;
                // GasDetected entry action (CD-GA-FR4): ins = intensity(gs)
                this.ins = sensor.intensity(this.gs);
            }
        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: autonomous -> Reading
            currentMode = GasAnalysisMode.Reading;
        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (intensityAboveThr) {
                // CD-GA-Beh6: -> j1, send stop
                movement.receive(new chemdetector.event.MovementInputEvent.Stop());
                currentMode = GasAnalysisMode.J1;
            } else {
                // CD-GA-Beh7: anl = location(gs); send turn ! anl; -> Reading
                this.anl = sensor.location(this.gs);
                movement.receive(new chemdetector.event.MovementInputEvent.Turn(this.anl));
                currentMode = GasAnalysisMode.Reading;
            }
        } else if (currentMode == GasAnalysisMode.J1) {
            // Final state — no outgoing transitions.
            currentMode = GasAnalysisMode.J1;
        }
    }
}
