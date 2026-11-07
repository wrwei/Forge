package chemical_detector;

import chemical_detector.actuator.GasAnalysisActuator;
import chemical_detector.actuator.VehicleActuator;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.sensor.GasAnalysisSensor;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * The Chemical Detector (CD-ARCH1, CD-ARCH2): the Vehicle's sensing and
 * actuation surface and the two reasoning subsystems. The Vehicle's gas and
 * obstacle events go to the subsystems; the gas-analysis subsystem talks to
 * the movement subsystem only through turn, stop and resume; the movement
 * subsystem signals the Vehicle with flag.
 */
public final class ChemicalDetectorSystem {

    private final GasAnalysisActuator commandLink;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    /**
     * @param gasAnalysisSensor the functions over gas readings
     * @param odometerSensor    the Vehicle's odometer
     * @param timer             the time source
     * @param vehicle           the Vehicle's motion operations and signals
     */
    public ChemicalDetectorSystem(GasAnalysisSensor gasAnalysisSensor, OdometerSensor odometerSensor,
                                  Clock timer, VehicleActuator vehicle) {
        this.commandLink = new GasAnalysisActuator();
        this.gasAnalysis = new GasAnalysisController(gasAnalysisSensor, commandLink);
        this.movement = new MovementController(odometerSensor, timer, vehicle);
    }

    /**
     * One sense-analyse-act cycle. {@code vehicleEvent} is the Vehicle's event
     * of this cycle (gas or obstacle), or {@code null} if there is none. The
     * gas-analysis subsystem steps first; a command it sends is delivered to
     * the movement subsystem (after one step that lets a pending autonomous
     * movement transition complete), and then the movement subsystem steps on
     * the Vehicle's event. A gas reading that arrives while a previous one is
     * still being analysed is not processed.
     */
    public void cycle(InputEvent vehicleEvent) {
        int sentBefore = commandLink.sentCount();
        gasAnalysis.step(vehicleEvent);
        if (commandLink.sentCount() > sentBefore) {
            movement.step(null);
            deliver(commandLink.lastSent());
        }
        movement.step(vehicleEvent);
    }

    /** The gas-analysis subsystem. */
    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    /** The movement subsystem. */
    public MovementController movement() {
        return movement;
    }

    private void deliver(OutputEvent command) {
        if (command instanceof OutputEvent.Turn) {
            OutputEvent.Turn turnCommand = (OutputEvent.Turn) command;
            movement.step(new InputEvent.Turn(turnCommand.direction()));
        } else if (command instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (command instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        }
    }
}
