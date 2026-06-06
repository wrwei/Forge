package chemdetector.controller;

import chemdetector.actuator.Actuator;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Status;
import chemdetector.event.InputEvent;
import chemdetector.event.OutputEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.Sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem state machine.
 *
 * States   : Reading, Analysis, NoGas, GasDetected, Final.
 * Init     : Reading (CD-GA-Beh1).
 * Variables: gs, sts, ins, anl (CD-GA-Var1..4).
 *
 * The controller follows the single-method, mode-nested if-else
 * pattern. Each transition action that the requirements describe as
 * a target-state entry action is placed on the incoming transition
 * (there is only one incoming transition per affected state, so the
 * M2M's common-incoming-action heuristic lifts it to entry).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final Sensor sensor;
    private final Actuator actuator;

    /** CD-GA-Var1 — most recent gas reading. */
    private List<GasSensor> gs = new ArrayList<>();

    /** CD-GA-Var2 — classification result for the current reading. */
    private Status sts = Status.noGas;

    /** CD-GA-Var3 — peak intensity of the current reading. */
    @RoboChartType("real")
    private double ins;

    /** CD-GA-Var4 — direction of the strongest signal. */
    private Angle anl = Angle.Front;

    public GasAnalysisController(Sensor sensor, Actuator actuator) {
        this.sensor = sensor;
        this.actuator = actuator;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
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

    public void step(InputEvent event) {
        // --- Named boolean predicates ---
        boolean stsNoGas = sts == Status.noGas;
        boolean stsGasD = sts == Status.gasD;
        boolean goreqInsThr = sensor.goreq(ins, Constants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2 — gas ? gs ; lifted entry action of Analysis:
            //              sts = analysis(gs)
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas ge = (InputEvent.Gas) event;
                gs = ge.gs();
                sts = sensor.analysis(gs);
                currentMode = GasAnalysisMode.Analysis;
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4 — sts == noGas, send resume, go to NoGas
            if (stsNoGas) {
                actuator.send(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            }
            // CD-GA-Beh5 — sts == gasD, go to GasDetected
            // Lifted entry action of GasDetected: ins = intensity(gs)
            else if (stsGasD) {
                ins = sensor.intensity(gs);
                currentMode = GasAnalysisMode.GasDetected;
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3 — autonomous back to Reading
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6 — goreq(ins, thr): send stop, go to Final
            if (goreqInsThr) {
                actuator.send(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Final;
            }
            // CD-GA-Beh7 — !goreq(ins, thr): anl = location(gs); send turn ! anl; back to Reading
            else if (!goreqInsThr) {
                anl = sensor.location(gs);
                actuator.send(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }
        }
    }
}
