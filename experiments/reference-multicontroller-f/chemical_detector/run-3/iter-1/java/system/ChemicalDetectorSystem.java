package chemical_detector.system;

import chemical_detector.actuator.Actuator;
import chemical_detector.actuator.Vehicle;
import chemical_detector.annotation.RoboChartType;
import chemical_detector.controller.GasAnalysisController;
import chemical_detector.controller.MovementController;
import chemical_detector.event.InputEvent;
import chemical_detector.event.OutputEvent;
import chemical_detector.operation.ChangeDirection;
import chemical_detector.sensor.ChemSensor;
import chemical_detector.timing.Clock;

/**
 * Wires the gas-analysis and movement subsystems to the Vehicle. The two subsystems
 * communicate only through turn, stop and resume; flag is delivered to the Vehicle.
 */
public final class ChemicalDetectorSystem {

    private final ChemSensor sensor = new ChemSensor();
    private final Vehicle vehicle = new Vehicle();
    private final Clock timer = new Clock();
    private final Actuator actuator = new Actuator();
    private final GasAnalysisController gasAnalysis;
    private final MovementController movement;

    public ChemicalDetectorSystem() {
        this.gasAnalysis = new GasAnalysisController(sensor, actuator);
        this.movement = new MovementController(sensor, vehicle, new ChangeDirection(vehicle), actuator, timer);
    }

    /** Runs one cycle of both subsystems on an environment event, then delivers their outputs. */
    public void cycle(InputEvent input) {
        if (input instanceof InputEvent.Tick) {
            timer.advance(1);
        }
        gasAnalysis.step(input);
        movement.step(input);
        var pending = actuator.drain();
        while (!pending.isEmpty()) {
            for (var output : pending) {
                deliver(output);
            }
            pending = actuator.drain();
        }
    }

    /** Records the latest odometer sample from the Vehicle. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        sensor.updateOdometer(distance);
    }

    public GasAnalysisController gasAnalysis() {
        return gasAnalysis;
    }

    public MovementController movement() {
        return movement;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    private void deliver(OutputEvent output) {
        if (output instanceof OutputEvent.Turn) {
            OutputEvent.Turn turnOutput = (OutputEvent.Turn) output;
            movement.step(new InputEvent.Turn(turnOutput.angle()));
        } else if (output instanceof OutputEvent.Stop) {
            movement.step(new InputEvent.Stop());
        } else if (output instanceof OutputEvent.Resume) {
            movement.step(new InputEvent.Resume());
        } else if (output instanceof OutputEvent.Flag) {
            vehicle.reportSourceFound();
        }
    }
}
