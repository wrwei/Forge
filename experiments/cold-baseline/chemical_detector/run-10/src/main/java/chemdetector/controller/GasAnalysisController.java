package chemdetector.controller;

import java.util.ArrayList;
import java.util.List;
import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import chemdetector.event.GasAnalysisInputEvent;
import chemdetector.event.GasAnalysisOutputEvent;
import chemdetector.sensor.Sensor;

/**
 * Gas-analysis subsystem controller. Implements the state machine
 * specified by CD-GA-FR1..4 and CD-GA-Beh1..7 in the mode-nested
 * if-else pattern required for RoboChart model extraction.
 *
 * <p>State variables: {@code gs} (CD-GA-Var1), {@code sts} (CD-GA-Var2),
 * {@code ins} (CD-GA-Var3), {@code anl} (CD-GA-Var4).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final Sensor sensor;
    private final EventBus bus;

    private List<GasSensor> gs;
    private Status sts;
    private Intensity ins;
    private Angle anl;

    public GasAnalysisController(Sensor sensor, EventBus bus) {
        this.sensor = sensor;
        this.bus = bus;
        this.gs = new ArrayList<>();
        this.sts = Status.noGas;
        this.ins = new Intensity(0.0);
        this.anl = Angle.Front;
    }

    public GasAnalysisMode currentMode() {
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

    public List<GasSensor> gs() {
        return gs;
    }

    public void step(GasAnalysisInputEvent event) {
        // Named boolean predicates (CRITICAL: simple comparisons only, no ternaries)
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean intensityAtOrAboveThr = Intensity.goreq(ins, Constants.thr);

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: Reading -> Analysis on gas ? gs
            if (event instanceof GasAnalysisInputEvent.Gas) {
                GasAnalysisInputEvent.Gas g = (GasAnalysisInputEvent.Gas) event;
                gs = g.reading();
                sensor.update(gs);
                // Entry action of Analysis (CD-GA-FR3): sts = analysis(gs)
                sts = sensor.analysis(gs);
                currentMode = GasAnalysisMode.Analysis;
            }
        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: Analysis -> NoGas if sts == noGas; send resume
            if (stsIsNoGas) {
                bus.publish(new GasAnalysisOutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            }
            // CD-GA-Beh5: Analysis -> GasDetected if sts == gasD
            else if (stsIsGasD) {
                // Entry action of GasDetected (CD-GA-FR4): ins = intensity(gs)
                ins = sensor.intensity(gs);
                currentMode = GasAnalysisMode.GasDetected;
            }
        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: NoGas -> Reading (autonomous)
            currentMode = GasAnalysisMode.Reading;
        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: GasDetected -> Final if goreq(ins, thr); send stop
            if (intensityAtOrAboveThr) {
                bus.publish(new GasAnalysisOutputEvent.Stop());
                currentMode = GasAnalysisMode.Final;
            }
            // CD-GA-Beh7: GasDetected -> Reading otherwise;
            // anl = location(gs); send turn ! anl
            else {
                anl = sensor.location(gs);
                bus.publish(new GasAnalysisOutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }
        } else if (currentMode == GasAnalysisMode.Final) {
            // Terminal state — no outgoing transitions.
        }
    }

    @RoboChartType("nat")
    public int gsSize() {
        return gs.size();
    }
}
