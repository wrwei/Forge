package chemical_detector.controller;

import chemical_detector.actuator.CommandChannel;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.ChemConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.Sensor;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to keep searching (resume), to steer towards the strongest signal
 * (turn), or that the chemical source has been found (stop).
 */
public final class GasAnalysis {

    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final Sensor sensor;
    private final CommandChannel commands;
    private List<GasSample> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysis(Sensor sensor, CommandChannel commands) {
        this.sensor = sensor;
        this.commands = commands;
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    public List<GasSample> gs() {
        return gs;
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

    /**
     * Runs one control cycle. {@code event} is the event received in this cycle,
     * or {@code null} for a cycle in which no event arrived.
     */
    public void step(InputEvent event) {
        boolean gasPresent = sts == Status.gasD;
        boolean peakAtOrAboveThr = sensor.goreq(ins, ChemConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas reading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Analysis;
                gs = reading.readings();
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (!gasPresent) {
                currentMode = GasAnalysisMode.NoGas;
                commands.apply(new OutputEvent.Resume());
            } else if (gasPresent) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Concluded;
                commands.apply(new OutputEvent.Stop());
            } else if (!peakAtOrAboveThr) {
                currentMode = GasAnalysisMode.Reading;
                anl = sensor.location(gs);
                commands.apply(new OutputEvent.Turn(anl));
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas reading = (InputEvent.Gas) event;
                currentMode = GasAnalysisMode.Concluded;
                gs = reading.readings();
            }
        }
    }
}
