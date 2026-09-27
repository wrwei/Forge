package chemical_detector;

import chemical_detector.actuator.MovementCommandChannel;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.GasAnalysisEvent;
import chemical_detector.event.MovementEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Clock;
import chemical_detector.sensor.GasSensorService;
import chemical_detector.sensor.OdometerSensor;

/**
 * The Chemical Detector: a gas-analysis subsystem and a movement subsystem around a
 * Vehicle, linked only by the turn, stop and resume commands.
 */
public final class ChemicalDetectorSystem {

    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem(GasSensorService gasSensor, OdometerSensor odometer, Vehicle vehicle, Clock timer) {
        var channel = new MovementCommandChannel();
        this.movement = new MovementController(vehicle, new ChangeDirection(vehicle), odometer, timer);
        channel.connect(this.movement);
        this.gasAnalysis = new GasAnalysisController(gasSensor, channel);
    }

    /**
     * Runs one sense-analyse-act cycle.
     *
     * @param gasEvent      the gas reading received this cycle, or {@code null}
     * @param platformEvent the obstacle event received this cycle, or {@code null}
     */
    public void cycle(GasAnalysisEvent gasEvent, MovementEvent.Obstacle platformEvent) {
        gasAnalysis.step(gasEvent);
        movement.step(platformEvent);
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }
}
