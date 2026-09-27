package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.domain.Chem;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.GasSensorFunctions;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * The Chemical Detector: a Vehicle driven by the gas-analysis and movement subsystems,
 * which communicate only through turn, stop and resume. The movement subsystem's flag
 * is delivered to the Vehicle.
 */
public final class ChemicalDetectorSystem {

    private final Actuator gasAnalysisOutput = new Actuator();
    private final Actuator movementOutput = new Actuator();
    private final Vehicle vehicle;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem(Chem target, Vehicle vehicle, OdometerSensor odometer, Clock timer) {
        this.vehicle = vehicle;
        this.gasAnalysis = new GasAnalysisController(new GasSensorFunctions(target), gasAnalysisOutput);
        this.movement = new MovementController(vehicle, odometer, new ChangeDirection(vehicle), timer,
                movementOutput);
    }

    /**
     * Runs one control cycle of both subsystems. {@code event} is the Vehicle event of
     * this cycle ({@code Gas} or {@code Obstacle}), or {@code null} if none.
     */
    public void cycle(InputEvent event) {
        InputEvent gasAnalysisInput = null;
        InputEvent movementInput = null;
        if (event instanceof InputEvent.Gas) {
            gasAnalysisInput = event;
        } else if (event instanceof InputEvent.Obstacle) {
            movementInput = event;
        }
        gasAnalysis.step(gasAnalysisInput);
        movement.step(movementInput);
        if (gasAnalysisOutput.hasPending()) {
            movement.step(toMovementInput(gasAnalysisOutput.take()));
        }
        if (movementOutput.hasPending()) {
            deliverToVehicle(movementOutput.take());
        }
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    private static InputEvent toMovementInput(OutputEvent output) {
        InputEvent input = null;
        if (output instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) output;
            input = new InputEvent.Turn(turn.a());
        } else if (output instanceof OutputEvent.Stop) {
            input = new InputEvent.Stop();
        } else if (output instanceof OutputEvent.Resume) {
            input = new InputEvent.Resume();
        }
        return input;
    }

    private void deliverToVehicle(OutputEvent output) {
        if (output instanceof OutputEvent.Flag) {
            vehicle.raiseFlag();
        }
    }
}
