package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.domain.Chem;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.GasSensorArray;
import chemical_detector.sensor.OdometerSensor;
import chemical_detector.timing.Clock;

/**
 * Composes the Vehicle, the gas-analysis subsystem and the movement subsystem.
 * The two subsystems communicate only through turn, stop and resume; flag goes to the Vehicle.
 */
public final class ChemicalDetector {

    private final Vehicle vehicle;
    private final OdometerSensor odometerSensor;
    private final Clock timer;
    private final Actuator gasAnalysisOutput;
    private final Actuator movementOutput;
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetector(Chem targetChemical) {
        this.vehicle = new Vehicle();
        this.odometerSensor = new OdometerSensor();
        this.timer = new Clock();
        this.gasAnalysisOutput = new Actuator();
        this.movementOutput = new Actuator();
        this.gasAnalysis = new GasAnalysisController(new GasSensorArray(targetChemical), gasAnalysisOutput);
        this.movement = new MovementController(vehicle, odometerSensor, new ChangeDirection(vehicle),
                movementOutput, timer);
    }

    /**
     * Runs one control cycle: the gas-analysis subsystem handles {@code input}, its
     * turn/stop/resume outputs are delivered to the movement subsystem, then the
     * movement subsystem handles {@code input}; a flag is delivered to the Vehicle.
     */
    public void cycle(InputEvent input) {
        gasAnalysis.step(input);
        for (var out : gasAnalysisOutput.drain()) {
            forwardToMovement(out);
        }
        movement.step(input);
        for (var out : movementOutput.drain()) {
            if (out instanceof OutputEvent.Flag) {
                vehicle.reportSourceFound();
            }
        }
    }

    private void forwardToMovement(OutputEvent out) {
        if (out instanceof OutputEvent.Turn) {
            OutputEvent.Turn turn = (OutputEvent.Turn) out;
            movement.step(new InputEvent.Turn(turn.a()));
        } else if (out instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (out instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        }
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public OdometerSensor odometerSensor() {
        return odometerSensor;
    }

    public Clock timer() {
        return timer;
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }
}
