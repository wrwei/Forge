package chemdetector.controller;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import chemdetector.event.GAInputEvent;
import chemdetector.event.SharedEventBus;
import chemdetector.sensor.GasFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis controller (CD-ARCH2 subsystem 2). Classifies the
 * most recent gas reading and decides whether to keep searching,
 * change direction towards a stronger signal, or stop because the
 * chemical source has been confirmed.
 *
 * State machine per CD-GA-FR1..4 and CD-GA-Beh1..7.
 */
public final class GasAnalysisController {

    private GAMode currentMode = GAMode.Reading;

    private final GasFunctions gasFunctions;

    private final SharedEventBus bus;

    private List<GasSensor> gs = new ArrayList<GasSensor>();

    private Status sts = Status.noGas;

    @RoboChartType("real")
    private double ins = 0.0;

    private Angle anl = Angle.Front;

    public GasAnalysisController(GasFunctions gasFunctions, SharedEventBus bus) {
        this.gasFunctions = gasFunctions;
        this.bus = bus;
    }

    public GAMode currentMode() {
        return currentMode;
    }

    public void step(GAInputEvent event) {
        // --- Named boolean predicates (declared BEFORE the if-else chain) ---
        boolean stsIsGasD = this.sts == Status.gasD;
        boolean insAboveThr = gasFunctions.goreq(this.ins, Constants.THR);

        // --- Pure mode-nested if-else: every outer branch is currentMode == X ---
        if (currentMode == GAMode.Reading) {
            // CD-GA-Beh2: Reading -> Analysis on gas ? gs
            if (event instanceof GAInputEvent.Gas) {
                GAInputEvent.Gas ge = (GAInputEvent.Gas) event;
                this.gs = ge.reading();
                currentMode = GAMode.Analysis;
                // entry action of Analysis: sts = analysis(gs)
                this.sts = gasFunctions.analysis(this.gs);
            }

        } else if (currentMode == GAMode.Analysis) {
            // CD-GA-Beh5: Analysis -> GasDetected when sts == gasD
            if (stsIsGasD) {
                currentMode = GAMode.GasDetected;
                // entry action of GasDetected: ins = intensity(gs)
                this.ins = gasFunctions.intensity(this.gs);
            }
            // CD-GA-Beh4: Analysis -> NoGas otherwise (sts == noGas);
            //   action: send resume.
            // Bare-precondition fallback for deadlock-freedom (Status is binary).
            else {
                bus.resume();
                currentMode = GAMode.NoGas;
            }

        } else if (currentMode == GAMode.NoGas) {
            // CD-GA-Beh3: NoGas -> Reading (autonomous, bare precondition)
            currentMode = GAMode.Reading;

        } else if (currentMode == GAMode.GasDetected) {
            // CD-GA-Beh6: GasDetected -> Final when goreq(ins, thr); action: send stop
            if (insAboveThr) {
                bus.stop();
                currentMode = GAMode.Final;
            }
            // CD-GA-Beh7: GasDetected -> Reading otherwise (NOT goreq(ins, thr));
            //   action: anl = location(gs); send turn ! anl.
            // Bare-precondition fallback for deadlock-freedom (predicate is binary).
            else {
                this.anl = gasFunctions.location(this.gs);
                bus.turn(this.anl);
                currentMode = GAMode.Reading;
            }
        }
    }

    /** Test/inspection accessors. */
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

    public List<GasSensor> gs() {
        return gs;
    }
}
