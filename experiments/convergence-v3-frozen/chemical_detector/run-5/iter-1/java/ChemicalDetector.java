package chemical_detector;

import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementCommands;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.GasAnalysisFunctions;
import chemical_detector.sensor.VehicleSensors;
import chemical_detector.timing.Clock;

/**
 * The Chemical Detector system: wires the Vehicle to the gas-analysis and
 * movement subsystems and runs the sense-analyse-act loop. Vehicle events are
 * routed to the subsystem that consumes them; turn, stop and resume emitted by
 * the gas-analysis subsystem are delivered to the movement subsystem; each
 * subsystem is then stepped until it rests in a mode that waits for an event.
 */
public final class ChemicalDetector {

    /** Upper bound on consecutive autonomous steps taken to settle a subsystem. */
    private static final int SETTLE_LIMIT = 8;

    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;
    private final MovementCommands movementCommands;

    public ChemicalDetector(Vehicle vehicle, VehicleSensors sensors, Clock clock) {
        this.movementCommands = new MovementCommands();
        this.gasAnalysis = new GasAnalysisController(new GasAnalysisFunctions(), movementCommands);
        this.movement = new MovementController(vehicle, sensors, new ChangeDirection(vehicle), clock);
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    /** Handles one event from the Vehicle or the control-cycle tick. */
    public void dispatch(InputEvent event) {
        if (event instanceof InputEvent.Gas) {
            gasAnalysis.step(event);
            settleGasAnalysis();
        } else if (event instanceof InputEvent.Tick) {
            gasAnalysis.step(event);
            settleGasAnalysis();
            movement.step(event);
            settleMovement();
        } else {
            movement.step(event);
            settleMovement();
        }
    }

    private void settleGasAnalysis() {
        deliverCommands();
        for (var i = 0; i < SETTLE_LIMIT && gasAnalysisIsAutonomous(); i++) {
            gasAnalysis.step(new InputEvent.Tick());
            deliverCommands();
        }
    }

    private void settleMovement() {
        for (var i = 0; i < SETTLE_LIMIT && movement.currentMode() == MovementMode.AVOIDING_AGAIN; i++) {
            movement.step(new InputEvent.Tick());
        }
    }

    private void deliverCommands() {
        for (var command : movementCommands.drain()) {
            movement.step(command);
            settleMovement();
        }
    }

    private boolean gasAnalysisIsAutonomous() {
        var mode = gasAnalysis.currentMode();
        return mode == GasAnalysisMode.ANALYSIS
                || mode == GasAnalysisMode.NO_GAS
                || mode == GasAnalysisMode.GAS_DETECTED;
    }
}
