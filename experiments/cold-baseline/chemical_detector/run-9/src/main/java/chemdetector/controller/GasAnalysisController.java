package chemdetector.controller;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Status;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.operation.Analysis;
import chemdetector.operation.IntensityCompute;
import chemdetector.operation.LocationCompute;
import chemdetector.sensor.GasSensorService;
import java.util.ArrayList;
import java.util.List;

/**
 * Gas-analysis subsystem controller. Mode-nested if-else state machine over
 * {@link GasAnalysisMode}. Emits inter-controller events (turn / stop / resume)
 * to the movement controller via the {@link MovementController#enqueue} bridge.
 *
 * <p>Implements requirements CD-GA-FR1..4, CD-GA-Beh1..7, CD-GA-Var1..4.</p>
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;

    private final GasSensorService sensor;
    private final Analysis analysis;
    private final IntensityCompute intensityCompute;
    private final LocationCompute locationCompute;
    private final MovementController movement;

    /** CD-GA-Var1: most recent reading. */
    private List<GasSensor> gs = new ArrayList<>();

    /** CD-GA-Var2: outcome of analysis. */
    private Status sts = Status.noGas;

    /** CD-GA-Var3: peak intensity of the current reading. */
    @RoboChartType("real")
    private double ins = 0.0;

    /** CD-GA-Var4: direction towards strongest signal. */
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorService sensor,
                                 Analysis analysis,
                                 IntensityCompute intensityCompute,
                                 LocationCompute locationCompute,
                                 MovementController movement) {
        this.sensor = sensor;
        this.analysis = analysis;
        this.intensityCompute = intensityCompute;
        this.locationCompute = locationCompute;
        this.movement = movement;
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

    public void step(GasAnalysisEvent event) {
        // Named boolean predicates — atomic guard conditions
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean peakAboveThreshold = sensor.goreq(ins, Constants.thr.value());

        if (currentMode == GasAnalysisMode.Reading) {
            // CD-GA-Beh2: Reading -> Analysis on gas event
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                gs = g.reading();
                sensor.update(gs);
                currentMode = GasAnalysisMode.Analysis;
                // Entry of Analysis: sts = analysis(gs)
                analysis.setInput(gs);
                analysis.compute();
                sts = analysis.sts();
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            // CD-GA-Beh4: Analysis -> NoGas when sts == noGas; emit resume
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                movement.enqueue(new MovementEvent.Resume());
            }
            // CD-GA-Beh5: Analysis -> GasDetected when sts == gasD
            else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                // Entry of GasDetected: ins = intensity(gs)
                intensityCompute.setInput(gs);
                intensityCompute.compute();
                ins = intensityCompute.ins();
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            // CD-GA-Beh3: NoGas -> Reading autonomously
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            // CD-GA-Beh6: GasDetected -> Final when ins >= thr; emit stop
            if (peakAboveThreshold) {
                currentMode = GasAnalysisMode.Final;
                movement.enqueue(new MovementEvent.Stop());
            }
            // CD-GA-Beh7: GasDetected -> Reading when ins < thr; turn ! location(gs)
            else if (!peakAboveThreshold) {
                locationCompute.setInput(gs);
                locationCompute.compute();
                anl = locationCompute.anl();
                currentMode = GasAnalysisMode.Reading;
                movement.enqueue(new MovementEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Final) {
            // Terminal state — no outgoing transitions
        }
    }
}
