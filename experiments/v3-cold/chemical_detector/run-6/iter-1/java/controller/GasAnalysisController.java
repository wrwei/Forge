package chemical_detector.controller;

import chemical_detector.ChemicalDetectorConstants;
import chemical_detector.actuator.CommandActuator;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.ChemicalSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * The gas-analysis subsystem (CD-ARCH2): classifies each gas reading and tells
 * the movement subsystem to keep searching (resume), to head for the strongest
 * signal (turn) or that the source is found (stop).
 */
public final class GasAnalysisController {
    private GasAnalysisMode currentMode = GasAnalysisMode.Reading;
    private final ChemicalSensor sensor;
    private final CommandActuator commands;

    private List<GasSensor> gs = new ArrayList<>();
    private Status sts = Status.noGas;
    @RoboChartType("nat") private int ins = 0;
    private Angle anl = Angle.Front;

    /**
     * @param sensor   the functions over gas readings
     * @param commands where the turn, stop and resume commands are issued
     */
    public GasAnalysisController(ChemicalSensor sensor, CommandActuator commands) {
        this.sensor = sensor;
        this.commands = commands;
    }

    /** The current state. */
    public GasAnalysisMode currentMode() {
        return currentMode;
    }

    /** One control cycle: takes at most one transition from the current state. */
    public void step(InputEvent event) {
        boolean classifiedNoGas = sts == Status.noGas;
        boolean peakAtOrAboveThr = sensor.goreq(ins, ChemicalDetectorConstants.THR);

        if (currentMode == GasAnalysisMode.Reading) {
            if (event instanceof InputEvent.Gas) {
                InputEvent.Gas gasReading = (InputEvent.Gas) event;
                gs = gasReading.reading();
                currentMode = GasAnalysisMode.Analysis;
                sts = sensor.analysis(gs);
            }

        } else if (currentMode == GasAnalysisMode.Analysis) {
            if (classifiedNoGas) {
                commands.apply(new OutputEvent.Resume());
                currentMode = GasAnalysisMode.NoGas;
            } else if (!classifiedNoGas) {
                currentMode = GasAnalysisMode.GasDetected;
                ins = sensor.intensity(gs);
            }

        } else if (currentMode == GasAnalysisMode.NoGas) {
            currentMode = GasAnalysisMode.Reading;

        } else if (currentMode == GasAnalysisMode.GasDetected) {
            if (peakAtOrAboveThr) {
                commands.apply(new OutputEvent.Stop());
                currentMode = GasAnalysisMode.Concluded;
            } else if (!peakAtOrAboveThr) {
                anl = sensor.location(gs);
                commands.apply(new OutputEvent.Turn(anl));
                currentMode = GasAnalysisMode.Reading;
            }

        } else if (currentMode == GasAnalysisMode.Concluded) {
            // Final: the search is over and no further readings are processed.
        }
    }
}
