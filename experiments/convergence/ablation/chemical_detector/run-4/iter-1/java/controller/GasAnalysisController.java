package chemdetector.controller;

import chemdetector.actuator.AnalysisRelay;
import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.CdConstants;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import chemdetector.event.GasAnalysisEvent;
import chemdetector.event.MovementEvent;
import chemdetector.mode.GasAnalysisMode;
import chemdetector.sensor.GasAnalyzer;
import java.util.List;

/**
 * The gas-analysis subsystem: classifies each gas reading and decides
 * whether to keep searching (resume), steer toward a stronger signal
 * (turn), or stop because the chemical source has been found (stop).
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final GasAnalyzer analyzer;
    private final AnalysisRelay relay;

    /** Most recent gas reading; initially empty. */
    private List<GasSensor> gs = List.of();
    /** Classification of the most recent reading. */
    private Status sts = Status.noGas;
    /** Peak intensity of the most recent reading. */
    @RoboChartType("real")
    private double ins;
    /** Direction of the strongest detected signal. */
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasAnalyzer analyzer, AnalysisRelay relay) {
        this.analyzer = analyzer;
        this.relay = relay;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /**
     * Evaluates all gas-analysis transitions for one control cycle.
     * Event-triggered transitions consume {@code event}; autonomous
     * transitions fire on guards alone.
     */
    public void step(GasAnalysisEvent event) {
        // --- Named boolean predicates ---
        boolean stsIsNoGas = sts == Status.noGas;
        boolean stsIsGasD = sts == Status.gasD;
        boolean insAtOrAboveThr = ins >= CdConstants.THR;

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.reading();
                currentMode = GasAnalysisMode.Analysis;
                this.sts = analyzer.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (stsIsNoGas) {
                currentMode = GasAnalysisMode.NoGas;
                relay.apply(new MovementEvent.Resume());
            } else if (stsIsGasD) {
                currentMode = GasAnalysisMode.GasDetected;
                this.ins = analyzer.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (insAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                relay.apply(new MovementEvent.Stop());
            } else if (!insAtOrAboveThr) {
                this.anl = analyzer.location(gs);
                relay.apply(new MovementEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof GasAnalysisEvent.Gas) {
                currentMode = GasAnalysisMode.Concluded;
            }
        }
    }
}
