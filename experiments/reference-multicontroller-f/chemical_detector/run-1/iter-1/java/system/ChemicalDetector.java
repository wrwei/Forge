package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Loc;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import chemical_detector.sensor.ChemSensors;
import chemical_detector.sensor.Clock;
import java.util.List;

/**
 * The Chemical Detector: the Vehicle, the gas-analysis subsystem and the movement subsystem.
 * The two controllers communicate only through turn, stop and resume; movement sends flag
 * to the Vehicle.
 */
public final class ChemicalDetector {

    /** Cycles gas analysis needs to classify a reading and reach Reading or Final again. */
    private static final int ANALYSIS_CYCLES = 2;

    private final Vehicle vehicle;
    private final Actuator gasAnalysisOutput = new Actuator();
    private final Actuator movementOutput = new Actuator();
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetector(ChemSensors sensors, Vehicle vehicle, Clock timer) {
        this.vehicle = vehicle;
        this.gasAnalysis = new GasAnalysisController(sensors, gasAnalysisOutput);
        this.movement = new MovementController(sensors, vehicle, movementOutput, timer);
    }

    /** Delivers a gas reading from the Vehicle and runs its analysis to completion. */
    public void onGas(List<GasSensor> reading) {
        gasAnalysis.step(new InputEvent.Gas(List.copyOf(reading)));
        forwardAnalysisOutputs();
        for (int cycle = 0; cycle < ANALYSIS_CYCLES; cycle++) {
            gasAnalysis.step(null);
            forwardAnalysisOutputs();
        }
    }

    /** Delivers an obstacle report from the Vehicle. */
    public void onObstacle(Loc side) {
        deliverToMovement(new InputEvent.Obstacle(side));
    }

    /** Runs one movement cycle in which no event arrived. */
    public void cycle() {
        deliverToMovement(null);
    }

    public GasAnalysisMode gasAnalysisMode() {
        return gasAnalysis.currentMode();
    }

    public MovementMode movementMode() {
        return movement.currentMode();
    }

    private void forwardAnalysisOutputs() {
        for (var emitted : gasAnalysisOutput.drain()) {
            if (emitted instanceof OutputEvent.Turn) {
                OutputEvent.Turn turn = (OutputEvent.Turn) emitted;
                deliverToMovement(new InputEvent.Turn(turn.angle()));
            } else if (emitted instanceof OutputEvent.Stop) {
                deliverToMovement(new InputEvent.Stop());
            } else if (emitted instanceof OutputEvent.Resume) {
                deliverToMovement(new InputEvent.Resume());
            }
        }
    }

    private void deliverToMovement(InputEvent input) {
        movement.step(input);
        // A second, event-free cycle settles AvoidingAgain's autonomous decision.
        movement.step(null);
        for (var emitted : movementOutput.drain()) {
            if (emitted instanceof OutputEvent.Flag) {
                vehicle.flag();
            }
        }
    }
}
