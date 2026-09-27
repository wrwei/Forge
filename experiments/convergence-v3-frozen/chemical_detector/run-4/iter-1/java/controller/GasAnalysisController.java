package chemical_detector.controller;

import chemical_detector.actuator.MovementCommandChannel;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.constants.DetectorConstants;
import chemical_detector.data.Angle;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import chemical_detector.event.GasAnalysisEvent;
import chemical_detector.event.MovementEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.sensor.GasSensorService;
import java.util.List;

/**
 * Gas-analysis subsystem: classifies each gas reading and tells the movement
 * subsystem to resume searching, turn toward the strongest signal, or stop.
 *
 * <p>Once the source is confirmed the subsystem enters CONCLUDED, where further
 * readings are retained but no longer analysed and no further commands are sent.
 */
public final class GasAnalysisController {

    private GasAnalysisMode currentMode = GasAnalysisMode.READING;
    private final GasSensorService gasSensor;
    private final MovementCommandChannel movementChannel;

    private List<GasSensor> gs = List.of();
    private Status sts = Status.noGas;
    @RoboChartType("real")
    private double ins = 0.0;
    private Angle anl = Angle.Front;

    public GasAnalysisController(GasSensorService gasSensor, MovementCommandChannel movementChannel) {
        this.gasSensor = gasSensor;
        this.movementChannel = movementChannel;
    }

    /**
     * Executes one control cycle.
     *
     * @param event the gas event received this cycle, or {@code null} when none arrived
     */
    public void step(GasAnalysisEvent event) {
        boolean statusNoGas = sts == Status.noGas;
        boolean statusGasDetected = sts == Status.gasD;
        boolean intensityAtOrAboveThreshold = ins >= DetectorConstants.THR;

        if (currentMode == GasAnalysisMode.READING) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.reading();
                currentMode = GasAnalysisMode.ANALYSIS;
                this.sts = gasSensor.analysis(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.ANALYSIS) {
            if (statusNoGas) {
                movementChannel.deliver(new MovementEvent.Resume());
                currentMode = GasAnalysisMode.NO_GAS;
            } else if (statusGasDetected) {
                currentMode = GasAnalysisMode.GAS_DETECTED;
                this.ins = gasSensor.intensity(this.gs);
            }

        } else if (currentMode == GasAnalysisMode.NO_GAS) {
            currentMode = GasAnalysisMode.READING;

        } else if (currentMode == GasAnalysisMode.GAS_DETECTED) {
            if (intensityAtOrAboveThreshold) {
                movementChannel.deliver(new MovementEvent.Stop());
                currentMode = GasAnalysisMode.CONCLUDED;
            } else if (!intensityAtOrAboveThreshold) {
                this.anl = gasSensor.location(this.gs);
                movementChannel.deliver(new MovementEvent.Turn(this.anl));
                currentMode = GasAnalysisMode.READING;
            }

        } else if (currentMode == GasAnalysisMode.CONCLUDED) {
            if (event instanceof GasAnalysisEvent.Gas) {
                GasAnalysisEvent.Gas g = (GasAnalysisEvent.Gas) event;
                this.gs = g.reading();
                currentMode = GasAnalysisMode.CONCLUDED;
            }
        }
    }

    public GasAnalysisMode currentMode() {
        return currentMode;
    }
}
