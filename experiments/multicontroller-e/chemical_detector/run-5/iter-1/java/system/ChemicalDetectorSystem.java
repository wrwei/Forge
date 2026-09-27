package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.mode.GasAnalysisMode;
import chemical_detector.mode.MovementMode;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.Sensor;
import chemical_detector.timing.Clock;
import java.util.List;

/**
 * The Chemical Detector (CD-ARCH1, CD-ARCH2): wires the Vehicle to the gas-analysis and
 * movement subsystems. The two subsystems communicate only through turn, stop and resume;
 * the movement subsystem's flag is delivered to the Vehicle.
 */
public final class ChemicalDetectorSystem {

    private final Vehicle vehicle;
    private final Actuator gasAnalysisOutput = new Actuator();
    private final Actuator movementOutput = new Actuator();
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem(Vehicle vehicle, Sensor sensor, Clock timer) {
        this.vehicle = vehicle;
        this.gasAnalysis = new GasAnalysisController(sensor, gasAnalysisOutput);
        this.movement = new MovementController(sensor, movementOutput, vehicle,
                new ChangeDirection(vehicle), timer);
    }

    /**
     * Runs one sense-analyse-act cycle. {@code environmentEvent} is the Vehicle's gas or
     * obstacle event for this cycle, or {@code null} when the Vehicle reported nothing.
     */
    public void cycle(InputEvent environmentEvent) {
        gasAnalysis.step(environmentEvent);
        forward(gasAnalysisOutput.drain());
        movement.step(environmentEvent);
        deliver(movementOutput.drain());
    }

    public GasAnalysisMode gasAnalysisMode() {
        return gasAnalysis.currentMode();
    }

    public MovementMode movementMode() {
        return movement.currentMode();
    }

    private void forward(List<OutputEvent> shared) {
        for (var output : shared) {
            if (output instanceof OutputEvent.turn) {
                OutputEvent.turn command = (OutputEvent.turn) output;
                movement.step(new InputEvent.turn(command.angle()));
            } else if (output instanceof OutputEvent.stop) {
                movement.step(new InputEvent.stop());
            } else if (output instanceof OutputEvent.resume) {
                movement.step(new InputEvent.resume());
            }
            deliver(movementOutput.drain());
        }
    }

    private void deliver(List<OutputEvent> toVehicle) {
        for (var output : toVehicle) {
            if (output instanceof OutputEvent.flag) {
                vehicle.signalSourceFound();
            }
        }
    }
}
